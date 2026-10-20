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
    // Circuit breaker registries from YAML, with Micrometer meters and health indicators.
    implementation("io.github.resilience4j:resilience4j-spring-boot4:${property("resilience4j.version")}")

    "developmentOnly"("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("io.micrometer:micrometer-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:${property("archunit.version")}")
    testImplementation("io.karatelabs:karate-junit5:${property("karate.version")}")
}

tasks.named<BootJar>("bootJar") {
    archiveFileName = "${rootProject.name}.jar"
}

tasks.named<Jar>("jar") {
    enabled = false
}
