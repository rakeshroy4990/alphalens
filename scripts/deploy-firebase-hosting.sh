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
npm run build
firebase deploy --only hosting --project "${FIREBASE_PROJECT}"
cd ..
