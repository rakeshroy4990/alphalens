#!/usr/bin/env bash
# Shared helpers for AlphaLens local start scripts.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
COMPOSE_FILE="$ROOT_DIR/infrastructure/docker-compose.yml"

BACKEND_PORT="${BACKEND_PORT:-8088}"
UI_PORT="${UI_PORT:-5173}"
RUN_TESTS="${RUN_TESTS:-0}"

ensure_docker_host() {
  if [[ -n "${DOCKER_HOST:-}" ]]; then
    return
  fi
  local colima_sock="${HOME}/.colima/default/docker.sock"
  if [[ -S "$colima_sock" ]]; then
    export DOCKER_HOST="unix://${colima_sock}"
    echo "Using Colima Docker socket: $DOCKER_HOST"
  fi
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

prefer_local_postgres_unless_supabase_forced() {
  if [[ "${USE_SUPABASE:-0}" == "1" ]]; then
    return
  fi
  local url="${SPRING_DATASOURCE_URL:-}"
  if [[ "$url" == *"supabase.com"* ]]; then
    echo "Supabase URL is set, but USE_SUPABASE=1 is not. Using local Docker Postgres."
    echo "Export USE_SUPABASE=1 to force the remote project."
    export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/alphalens"
    export SPRING_DATASOURCE_USERNAME="alphalens"
    export SPRING_DATASOURCE_PASSWORD="alphalens"
  fi
}

start_postgres() {
  load_backend_env
  prefer_local_postgres_unless_supabase_forced
  if ! uses_local_postgres; then
    echo "Using remote PostgreSQL from SPRING_DATASOURCE_URL. Skipping local Docker."
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
    npm run dev -- --host 127.0.0.1 --port "$UI_PORT"
  ) &
  UI_PID=$!
}

start_backend() {
  echo "Starting backend on port $BACKEND_PORT..."
  (
    cd "$BACKEND_DIR"
    setup_gradle_java
    load_backend_env
    prefer_local_postgres_unless_supabase_forced
    local gradle_bin
    gradle_bin="$(resolve_gradle)"
    "$gradle_bin" bootRun --args="--server.port=$BACKEND_PORT"
  ) &
  BACKEND_PID=$!
}
