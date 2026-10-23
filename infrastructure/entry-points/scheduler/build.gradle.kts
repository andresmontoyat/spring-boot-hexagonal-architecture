dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.slf4j:slf4j-api")
    // Only the lock API; the JDBC LockProvider bean comes from the outbox SDK at runtime.
    implementation("net.javacrumbs.shedlock:shedlock-core:${property("shedlock.version")}")

    testImplementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:${property("shedlock.version")}")
    testImplementation("org.springframework:spring-jdbc")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.awaitility:awaitility")
    testRuntimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.slf4j:slf4j-simple")
}
