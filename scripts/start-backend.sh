#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/dev-common.sh"

ensure_env_file "$BACKEND_DIR/.env" "$BACKEND_DIR/.env.example"
start_postgres

echo "Preparing backend port..."
describe_port_owner "$BACKEND_PORT" "backend"
kill_stale_backend
kill_port "$BACKEND_PORT"

cleanup() {
  if [[ -n "${BACKEND_PID:-}" ]]; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT INT TERM

start_backend
wait_for_http "http://127.0.0.1:$BACKEND_PORT/api/health" "Backend" 90 || true

echo ""
echo "Server : http://127.0.0.1:$BACKEND_PORT"
echo "Health : http://127.0.0.1:$BACKEND_PORT/api/health"
echo "Press Ctrl+C to stop the backend."
echo ""

wait "$BACKEND_PID"
