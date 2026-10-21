import org.springframework.boot.gradle.tasks.bundling.BootJar

apply(plugin = "org.springframework.boot")

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":rest-controller"))
    implementation(project(":jdbc-repository"))
    implementation(project(":valkey-cache"))
    implementation(project(":metrics-publisher"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // OTLP export of metrics (micrometer-registry-otlp) and traces (Micrometer → OpenTelemetry bridge).
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    // Transactional outbox: PostgreSQL store + schedulers, and exactly one transport (Kafka).
    implementation(platform("io.codehunters:sdk-bom:${property("codehunters.sdk.bom.version")}"))
    implementation("io.codehunters:sdk-event-messaging-jdbc")
    implementation("io.codehunters:sdk-event-messaging-kafka")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    // Circuit breaker registries from YAML, with Micrometer meters and health indicators.
    implementation("io.github.resilience4j:resilience4j-spring-boot4:${property("resilience4j.version")}")

    "developmentOnly"("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-kafka")
    testImplementation("org.awaitility:awaitility")
    testImplementation("io.micrometer:micrometer-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:${property("archunit.version")}")
    testImplementation("io.karatelabs:karate-junit5:${property("karate.version")}")
    testImplementation("com.github.dasniko:testcontainers-keycloak:${property("testcontainers.keycloak.version")}")
}

// The committed realm is the single source for local compose and for tests.
sourceSets.named("test") {
    resources.srcDir(rootProject.file("docker/keycloak"))
}

tasks.named<Test>("test") {
    // The gateway test mounts the same KrakenD config that compose runs.
    systemProperty("krakend.config.dir", rootProject.file("docker/krakend").absolutePath)
    inputs.dir(rootProject.file("docker/krakend"))
}

// Run from the repository root so spring.docker.compose.file (docker/docker-compose.yaml) resolves.
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    workingDir = rootDir
}

tasks.named<BootJar>("bootJar") {
    archiveFileName = "${rootProject.name}.jar"
}

tasks.named<Jar>("jar") {
    enabled = false
}
