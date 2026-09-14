#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/dev-common.sh"

prepare_frontend

echo "Preparing UI port..."
describe_port_owner "$UI_PORT" "ui"
kill_port "$UI_PORT"

cleanup() {
  if [[ -n "${UI_PID:-}" ]]; then
    kill "$UI_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT INT TERM

start_frontend

echo ""
echo "UI : http://localhost:$UI_PORT"
echo "The Vite proxy expects the API on http://127.0.0.1:${BACKEND_PORT}."
echo "Press Ctrl+C to stop the UI."
echo ""

wait "$UI_PID"
