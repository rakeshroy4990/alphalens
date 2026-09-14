#!/bin/bash
set -euo pipefail

PORT="${PORT:-8080}"
BACKEND_PORT="${BACKEND_PORT:-8088}"
JAVA_OPTS="${JAVA_OPTS:--XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom}"

sed -e "s/__PORT__/${PORT}/g" -e "s/__BACKEND_PORT__/${BACKEND_PORT}/g" \
    /etc/nginx/alphalens.conf.template > /etc/nginx/nginx.conf
nginx -t

echo "Starting Java on 127.0.0.1:${BACKEND_PORT}; nginx on :${PORT}"
java ${JAVA_OPTS} -jar /app/backend.jar --server.port="${BACKEND_PORT}" &
JAVA_PID=$!

shutdown() {
    kill -TERM "${JAVA_PID}" 2>/dev/null || true
    nginx -s quit 2>/dev/null || true
    wait "${JAVA_PID}" 2>/dev/null || true
}
trap shutdown TERM INT

# Listen on $PORT immediately so Cloud Run can probe. Readiness must be HTTP
# GET /api/health — a TCP probe on 8080 succeeds before Java binds 8088.
nginx -g 'daemon off;' &
NGINX_PID=$!

wait -n "${JAVA_PID}" "${NGINX_PID}"
STATUS=$?
if ! kill -0 "${JAVA_PID}" 2>/dev/null; then
    echo "Java exited before nginx; upstream 127.0.0.1:${BACKEND_PORT} is down" >&2
fi
shutdown
exit "${STATUS}"
