#!/usr/bin/env bash
# Build the AlphaLens Google Cloud image locally. Optionally push to Artifact Registry.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck disable=SC1091
source "${ROOT}/infrastructure/gcp/project.env"
IMAGE_LOCAL="${IMAGE_LOCAL:-alphalens:latest}"
PROJECT_ID="${PROJECT_ID:-${GCP_PROJECT_ID}}"
LOCATION="${LOCATION:-${GCP_LOCATION}}"
REPOSITORY="${REPOSITORY:-${GCP_REPOSITORY}}"
IMAGE_NAME="${IMAGE_NAME:-${GCP_IMAGE}}"
TAG="${TAG:-$(git -C "${ROOT}" rev-parse --short HEAD 2>/dev/null || echo local)}"
# Cloud Run / typical GCE VMs need linux/amd64. Apple Silicon builds arm64 unless you set this.
PLATFORM="${PLATFORM:-}"

if [[ -S "${HOME}/.colima/default/docker.sock" && -z "${DOCKER_HOST:-}" ]]; then
  export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
fi

BUILD_ARGS=(
  -f "${ROOT}/infrastructure/gcp/Dockerfile"
  -t "${IMAGE_LOCAL}"
  -t "${IMAGE_LOCAL%:*}:${TAG}"
)
if [[ -n "${PLATFORM}" ]]; then
  BUILD_ARGS+=(--platform "${PLATFORM}")
fi
docker build "${BUILD_ARGS[@]}" "${ROOT}"

echo "Built ${IMAGE_LOCAL}"

if [[ "${PUSH:-0}" == "1" ]]; then
  if [[ -z "${PROJECT_ID}" ]]; then
    echo "Set PROJECT_ID to push (and optionally PUSH=1)." >&2
    exit 1
  fi
  REMOTE="${LOCATION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}"
  docker tag "${IMAGE_LOCAL}" "${REMOTE}:${TAG}"
  docker tag "${IMAGE_LOCAL}" "${REMOTE}:latest"
  docker push "${REMOTE}:${TAG}"
  docker push "${REMOTE}:latest"
  echo "Pushed ${REMOTE}:${TAG}"
fi
