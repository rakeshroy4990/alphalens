#!/bin/bash
set -euo pipefail

PORT="${PORT:-8080}"
BACKEND_PORT="${BACKEND_PORT:-8088}"
JAVA_OPTS="${JAVA_OPTS:--XX:MaxRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom}"

sed -e "s/__PORT__/${PORT}/g" -e "s/__BACKEND_PORT__/${BACKEND_PORT}/g" \
    /etc/nginx/alphalens.conf.template > /etc/nginx/nginx.conf
nginx -t

java ${JAVA_OPTS} -jar /app/backend.jar --server.port="${BACKEND_PORT}" &
JAVA_PID=$!

shutdown() {
    kill -TERM "${JAVA_PID}" 2>/dev/null || true
    nginx -s quit 2>/dev/null || true
    wait "${JAVA_PID}" 2>/dev/null || true
}
trap shutdown TERM INT

nginx -g 'daemon off;' &
NGINX_PID=$!

wait -n "${JAVA_PID}" "${NGINX_PID}"
STATUS=$?
shutdown
exit "${STATUS}"
