#!/usr/bin/env bash
# Point the local gcloud CLI at the AlphaLens Google Cloud project.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ -f "${ROOT}/infrastructure/gcp/project.env" ]]; then
  # shellcheck disable=SC1091
  source "${ROOT}/infrastructure/gcp/project.env"
elif [[ -f "${ROOT}/infrastructure/gcp/project.env.example" ]]; then
  # shellcheck disable=SC1091
  source "${ROOT}/infrastructure/gcp/project.env.example"
fi
GCP_PROJECT_ID="${GCP_PROJECT_ID:-alphalens-508509}"

if ! command -v gcloud >/dev/null 2>&1; then
  echo "gcloud CLI not found. Install: https://cloud.google.com/sdk/docs/install" >&2
  exit 1
fi

gcloud config set project "${GCP_PROJECT_ID}"
echo "gcloud project: $(gcloud config get-value project)"
echo "Console: https://console.cloud.google.com/home/dashboard?project=${GCP_PROJECT_ID}"
