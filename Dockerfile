# syntax=docker/dockerfile:1.7
#
# Packages the jar built by Gradle (`./gradlew :bootstrap:bootJar`); it does not compile.
# Building outside Docker keeps the GitHub Packages credentials out of the image build, and
# matches ci-templates java-artifact-docker-github (jar expected in bootstrap/build/libs).

# Both stages pin the same exact JRE (Java 21 LTS, Alpine); bump them together.

# Stage 1 — split the fat jar into Spring Boot layers so dependency layers are cached across
# releases and only the application layer changes on a code-only commit.
FROM eclipse-temurin:21.0.12.1_1-jre-alpine AS layers
WORKDIR /workspace
COPY bootstrap/build/libs/spring-boot-hexagonal-architecture.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Stage 2 — runtime: JRE only, no build tools, non-root.
FROM eclipse-temurin:21.0.12.1_1-jre-alpine

# Numeric UID/GID so Kubernetes `runAsNonRoot` can verify it without a passwd lookup.
RUN addgroup -S -g 10001 app && adduser -S -D -H -u 10001 -G app app

# Code stays root-owned and read-only for the runtime user: a compromised process cannot
# rewrite the application it runs.
WORKDIR /app
COPY --from=layers /workspace/extracted/dependencies/ ./
COPY --from=layers /workspace/extracted/spring-boot-loader/ ./
COPY --from=layers /workspace/extracted/snapshot-dependencies/ ./
COPY --from=layers /workspace/extracted/application/ ./

USER 10001:10001

# Heap sized from the container memory limit; crash fast on OOM so the orchestrator restarts.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    TZ=UTC \
    SPRING_PROFILES_ACTIVE=prod

# 8080 = API, 8081 = actuator (expose 8081 to the cluster only, never through the ingress).
EXPOSE 8080 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8081/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
