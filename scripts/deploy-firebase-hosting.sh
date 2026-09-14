#!/usr/bin/env bash
# Build the Vue app and deploy Firebase Hosting.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if ! command -v firebase >/dev/null 2>&1; then
  echo "Firebase CLI not found. Install it with: npm install -g firebase-tools" >&2
  echo "Then sign in: firebase login" >&2
  exit 1
fi

if [[ ! -f "${ROOT}/frontend/firebase.json" ]]; then
  echo "Missing frontend/firebase.json." >&2
  exit 1
fi

FIREBASE_PROJECT="${FIREBASE_PROJECT:-alphalens-a3cce}"

cd "${ROOT}/frontend"
if [[ -f .env ]]; then
  # shellcheck disable=SC1091
  set -a
  source .env
  set +a
fi
# Firebase has no /api proxy. /api is only valid for local Vite and the
# Cloud Run nginx image. A leftover start-dev export must not ship.
if [[ "${VITE_API_BASE_URL:-}" != https://* ]]; then
  echo "Firebase deploys need an absolute Cloud Run API URL, not '${VITE_API_BASE_URL:-}'." >&2
  echo "Set VITE_API_BASE_URL=https://alphalens-113523778150.asia-south1.run.app/api" >&2
  exit 1
fi
if [[ "${VITE_API_BASE_URL}" != */api ]]; then
  echo "VITE_API_BASE_URL must end with /api so the browser calls /api/instruments, not /instruments." >&2
  echo "Example: VITE_API_BASE_URL=https://alphalens-113523778150.asia-south1.run.app/api" >&2
  exit 1
fi
npm run build -- --mode production
firebase deploy --only hosting --project "${FIREBASE_PROJECT}"
cd ..
