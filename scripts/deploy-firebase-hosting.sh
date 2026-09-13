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
if [[ "${VITE_API_BASE_URL:-}" == https://* && "${VITE_API_BASE_URL}" != */api ]]; then
  echo "VITE_API_BASE_URL must end with /api so the browser calls /api/instruments, not /instruments." >&2
  echo "Example: VITE_API_BASE_URL=https://alphalens-113523778150.asia-south1.run.app/api" >&2
  exit 1
fi
npm run build
firebase deploy --only hosting --project "${FIREBASE_PROJECT}"
cd ..
