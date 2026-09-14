#!/usr/bin/env bash
# Shared helpers for AlphaLens local start scripts.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
COMPOSE_FILE="$ROOT_DIR/infrastructure/docker-compose.yml"

BACKEND_PORT="${BACKEND_PORT:-8088}"
UI_PORT="${UI_PORT:-5173}"
RUN_TESTS="${RUN_TESTS:-0}"

docker_daemon_reachable() {
  command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1
}

ensure_docker_host() {
  local colima_sock="${HOME}/.colima/default/docker.sock"

  if [[ -z "${DOCKER_HOST:-}" && -S "$colima_sock" ]]; then
    export DOCKER_HOST="unix://${colima_sock}"
    echo "Using Colima Docker socket: $DOCKER_HOST"
  fi

  if docker_daemon_reachable; then
    return
  fi

  if ! command -v colima >/dev/null 2>&1; then
    return
  fi

  echo "Docker daemon is not reachable. Starting Colima..."
  if ! colima start; then
    echo "Failed to start Colima. Start it manually with: colima start" >&2
    exit 1
  fi

  local i
  for i in $(seq 1 60); do
    if [[ -S "$colima_sock" ]]; then
      break
    fi
    sleep 1
  done

  if [[ -z "${DOCKER_HOST:-}" && -S "$colima_sock" ]]; then
    export DOCKER_HOST="unix://${colima_sock}"
    echo "Using Colima Docker socket: $DOCKER_HOST"
  fi

  for i in $(seq 1 30); do
    if docker_daemon_reachable; then
      echo "Colima Docker daemon is ready."
      return
    fi
    sleep 1
  done

  echo "Colima started, but the Docker daemon is still not reachable at ${DOCKER_HOST:-the default socket}." >&2
  exit 1
}

docker_compose() {
  ensure_docker_host
  if command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
  elif command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  else
    echo "Neither docker-compose nor 'docker compose' is available." >&2
    exit 1
  fi
}

ensure_env_file() {
  local dest="$1"
  local example="$2"
  if [[ ! -f "$dest" && -f "$example" ]]; then
    cp "$example" "$dest"
    echo "Created $dest from $(basename "$example")"
  fi
}

setup_gradle_java() {
  if [[ "${OSTYPE:-}" == darwin* ]] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local selected_java_home=""
    selected_java_home="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
    if [[ -z "$selected_java_home" ]]; then
      selected_java_home="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
    fi
    if [[ -n "$selected_java_home" ]]; then
      export JAVA_HOME="$selected_java_home"
      export PATH="$JAVA_HOME/bin:$PATH"
      echo "Using JAVA_HOME for Gradle: $JAVA_HOME"
    fi
  fi
}

resolve_gradle() {
  if [[ -x "$BACKEND_DIR/gradlew" ]]; then
    echo "$BACKEND_DIR/gradlew"
    return
  fi
  if command -v gradle >/dev/null 2>&1; then
    command -v gradle
    return
  fi
  local cached=""
  cached="$(find "$HOME/.gradle/wrapper/dists/gradle-8.12-bin" -path '*/gradle-8.12/bin/gradle' -type f 2>/dev/null | head -n 1 || true)"
  if [[ -n "$cached" && -x "$cached" ]]; then
    echo "$cached"
    return
  fi
  echo "No Gradle wrapper or gradle executable found." >&2
  exit 1
}

load_backend_env() {
  local env_file="$BACKEND_DIR/.env"
  if [[ -f "$env_file" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$env_file"
    set +a
    echo "Loaded backend env from $env_file"
  else
    echo "No backend env file found at $env_file"
  fi
}

describe_port_owner() {
  local port="$1"
  local label="${2:-service}"
  local pids

  pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "$pids" ]]; then
    echo "Port $port is free."
    return
  fi

  echo "Port $port currently used by:"
  for pid in $pids; do
    local cmd cwd
    cmd="$(ps -p "$pid" -o command= 2>/dev/null || true)"
    cwd="$(lsof -a -p "$pid" -d cwd -Fn 2>/dev/null | awk 'NR==1 {sub(/^n/, ""); print; exit}')"
    echo "  - PID $pid ($label)"
    if [[ -n "$cmd" ]]; then
      echo "    command: $cmd"
    fi
    if [[ -n "$cwd" ]]; then
      echo "    cwd    : $cwd"
    fi
  done
}

kill_matching_processes() {
  local pattern="$1"
  local label="$2"
  local pids

  pids="$(pgrep -f "$pattern" || true)"
  if [[ -z "$pids" ]]; then
    return
  fi

  echo "Stopping leftover $label: $pids"
  # Include stopped (Ctrl+Z) jobs — those never bind a port.
  kill -CONT $pids 2>/dev/null || true
  if ! kill $pids 2>/dev/null; then
    kill -9 $pids 2>/dev/null || true
  fi

  local i
  for i in $(seq 1 20); do
    if [[ -z "$(pgrep -f "$pattern" || true)" ]]; then
      echo "Stopped leftover $label."
      return
    fi
    sleep 0.25
  done

  pids="$(pgrep -f "$pattern" || true)"
  if [[ -n "$pids" ]]; then
    echo "Force-killing leftover $label: $pids" >&2
    kill -9 $pids 2>/dev/null || true
    sleep 0.5
  fi
}

# Spring that never reaches Tomcat is invisible to kill_port.
kill_stale_backend() {
  kill_matching_processes 'com.alphalens.AlphaLensApplication' 'AlphaLens Java processes'
  kill_matching_processes 'GradleWrapperMain bootRun' 'Gradle bootRun wrappers'
}

wait_for_http() {
  local url="$1"
  local label="$2"
  local attempts="${3:-90}"
  local i

  echo "Waiting for $label at $url ..."
  for i in $(seq 1 "$attempts"); do
    if curl -sf --max-time 2 "$url" >/dev/null 2>&1; then
      echo "$label is ready."
      return 0
    fi
    sleep 1
  done

  echo "$label did not become ready after ${attempts}s. The UI /api proxy will return 502." >&2
  return 1
}

kill_port() {
  local port="$1"
  local pids

  pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "$pids" ]]; then
    echo "Port $port is free."
    return
  fi

  echo "Killing process(es) on port $port: $pids"
  if ! kill $pids 2>/dev/null; then
    echo "SIGTERM failed on port $port; sending SIGKILL." >&2
    kill -9 $pids 2>/dev/null || true
  fi

  local i
  for i in $(seq 1 20); do
    if [[ -z "$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)" ]]; then
      echo "Port $port is now free."
      return
    fi
    sleep 0.25
  done

  pids="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -n "$pids" ]]; then
    echo "Force-killing remaining process(es) on port $port: $pids" >&2
    kill -9 $pids 2>/dev/null || true
    sleep 0.5
  fi
}

uses_local_postgres() {
  local url="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/alphalens}"
  [[ "$url" == *"localhost"* || "$url" == *"127.0.0.1"* ]]
}

log_datasource_target() {
  local url="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/alphalens}"
  local redacted
  redacted="$(printf '%s' "$url" | sed -E 's#(://)[^/@]+@#\1***@#')"
  echo "Using PostgreSQL from SPRING_DATASOURCE_URL: ${redacted}"
}

start_postgres() {
  load_backend_env
  log_datasource_target
  if ! uses_local_postgres; then
    echo "Remote PostgreSQL configured. Skipping local Docker."
    return
  fi

  echo "Starting local PostgreSQL..."
  docker_compose -f "$COMPOSE_FILE" up -d postgres

  echo "Waiting for PostgreSQL to become healthy..."
  local _
  for _ in $(seq 1 30); do
    if docker_compose -f "$COMPOSE_FILE" exec -T postgres pg_isready -U alphalens -d alphalens >/dev/null 2>&1; then
      echo "PostgreSQL is ready."
      return
    fi
    sleep 1
  done

  echo "PostgreSQL did not become ready in time." >&2
  exit 1
}

prepare_frontend() {
  ensure_env_file "$FRONTEND_DIR/.env" "$FRONTEND_DIR/.env.example"
  if [[ ! -d "$FRONTEND_DIR/node_modules" ]]; then
    echo "Installing frontend dependencies..."
    (
      cd "$FRONTEND_DIR"
      npm install
    )
  fi
}

start_frontend() {
  echo "Starting UI on port $UI_PORT..."
  (
    cd "$FRONTEND_DIR"
    # frontend/.env may hold the Cloud Run URL for Firebase builds. Vite
    # embeds VITE_* at startup, so local npm run dev must force the proxy.
    if [[ "${USE_REMOTE_API:-0}" != "1" ]]; then
      export VITE_API_BASE_URL="/api"
    fi
    echo "UI API base: ${VITE_API_BASE_URL:-/api} (local proxy → http://127.0.0.1:$BACKEND_PORT)"
    # Bind localhost (not 127.0.0.1): Google OAuth origins treat them as different sites.
    npm run dev -- --host localhost --port "$UI_PORT"
  ) &
  UI_PID=$!
}

start_backend() {
  echo "Starting backend on port $BACKEND_PORT..."
  (
    cd "$BACKEND_DIR"
    setup_gradle_java
    load_backend_env
    log_datasource_target
    local gradle_bin
    gradle_bin="$(resolve_gradle)"
    "$gradle_bin" bootRun --args="--server.port=$BACKEND_PORT"
  ) &
  BACKEND_PID=$!
}
