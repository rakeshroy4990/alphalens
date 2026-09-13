#!/usr/bin/env bash
# Point the local gcloud CLI at the AlphaLens Google Cloud project.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck disable=SC1091
source "${ROOT}/infrastructure/gcp/project.env"

if ! command -v gcloud >/dev/null 2>&1; then
  echo "gcloud CLI not found. Install: https://cloud.google.com/sdk/docs/install" >&2
  exit 1
fi

gcloud config set project "${GCP_PROJECT_ID}"
echo "gcloud project: $(gcloud config get-value project)"
echo "Console: https://console.cloud.google.com/home/dashboard?project=${GCP_PROJECT_ID}"
