#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/dev-common.sh"

ensure_env_file "$BACKEND_DIR/.env" "$BACKEND_DIR/.env.example"
prepare_frontend
start_postgres

echo "Preparing ports..."
describe_port_owner "$UI_PORT" "ui"
describe_port_owner "$BACKEND_PORT" "backend"
kill_stale_backend
kill_port "$UI_PORT"
kill_port "$BACKEND_PORT"

cleanup() {
  echo ""
  echo "Stopping services..."
  if [[ -n "${UI_PID:-}" ]]; then
    kill "$UI_PID" 2>/dev/null || true
  fi
  if [[ -n "${BACKEND_PID:-}" ]]; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT INT TERM

start_backend
wait_for_http "http://127.0.0.1:$BACKEND_PORT/api/health" "Backend" 90 || true
start_frontend

echo ""
echo "UI     : http://localhost:$UI_PORT"
echo "Server : http://127.0.0.1:$BACKEND_PORT"
echo "Health : http://127.0.0.1:$BACKEND_PORT/api/health"
echo "Browser API calls go to /api on the UI origin (Vite → local Spring)."
echo "Database: SPRING_DATASOURCE_URL from backend/.env (same cloud DB as Cloud Run)."
echo "Press Ctrl+C to stop all services."
echo "Tip    : BACKEND_PORT=8090 UI_PORT=5174 $0"
echo ""

wait
