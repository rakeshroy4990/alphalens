# Build from the repository root (same context as Cloud Build):
#   docker build -f backend/Dockerfile .
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY backend/gradlew backend/gradlew.bat backend/build.gradle backend/settings.gradle backend/gradle.properties ./backend/
COPY backend/gradle ./backend/gradle
COPY backend-auth-lib ./backend-auth-lib
RUN chmod +x backend/gradlew
COPY backend/src ./backend/src
WORKDIR /workspace/backend
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/backend/build/libs/*.jar /app/backend.jar
EXPOSE 8088
ENV PORT=8088 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/backend.jar"]
