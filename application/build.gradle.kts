plugins {
    // Shares the in-memory fakes with adapter modules that decorate application ports.
    `java-test-fixtures`
}

dependencies {
    implementation(project(":domain"))
    // Only the framework-free outbox API: OutboxWriter, OutboxEvent, OutboxEventFactory.
    implementation(platform("io.codehunters:sdk-bom:${property("codehunters.sdk.bom.version")}"))
    implementation("io.codehunters:sdk-event-messaging-core")

    testFixturesImplementation(project(":domain"))
    testFixturesImplementation(platform("io.codehunters:sdk-bom:${property("codehunters.sdk.bom.version")}"))
    testFixturesImplementation("io.codehunters:sdk-event-messaging-core")

    testImplementation("org.junit.jupiter:junit-jupiter-params")
}
