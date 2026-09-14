# AlphaLens all-in-one image for Google Cloud Run / GCE.
# Browser talks only to this image. Java stays on localhost inside the container.
# Market data remains MOCK until a signed license exists.

FROM eclipse-temurin:17-jdk-jammy AS backend-build
WORKDIR /src
COPY backend/gradlew backend/gradlew.bat backend/build.gradle backend/settings.gradle backend/gradle.properties ./
COPY backend/gradle ./gradle
RUN chmod +x gradlew
COPY backend/src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM node:22-alpine AS frontend-build
WORKDIR /src
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
# Relative /api is required here: nginx in this image proxies /api to Java.
# Firebase Hosting is a different build and must use the absolute Cloud Run URL.
ENV VITE_API_BASE_URL=/api
RUN npm run build

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends nginx \
    && rm -rf /var/lib/apt/lists/* \
    && rm -f /etc/nginx/sites-enabled/default

COPY infrastructure/gcp/nginx.conf.template /etc/nginx/alphalens.conf.template
COPY infrastructure/gcp/entrypoint.sh /entrypoint.sh
COPY --from=backend-build /src/build/libs/*.jar /app/backend.jar
COPY --from=frontend-build /src/dist /usr/share/nginx/html

RUN chmod +x /entrypoint.sh

WORKDIR /app
EXPOSE 8080
ENV PORT=8080 \
    BACKEND_PORT=8088 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

LABEL org.opencontainers.image.title="AlphaLens" \
      org.opencontainers.image.description="AlphaLens research UI and API. Mock market data." \
      org.opencontainers.image.source="https://github.com/alphalens/alphalens"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/${PORT:-8080} && printf 'GET /api/health HTTP/1.0\\r\\nHost: localhost\\r\\n\\r\\n' >&3 && grep -q 'HTTP/1.' <&3"]

ENTRYPOINT ["/entrypoint.sh"]
