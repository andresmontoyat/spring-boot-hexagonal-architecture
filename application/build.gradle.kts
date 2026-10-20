plugins {
    // Shares the in-memory fakes with adapter modules that decorate application ports.
    `java-test-fixtures`
}

dependencies {
    implementation(project(":domain"))

    testFixturesImplementation(project(":domain"))

    testImplementation("org.junit.jupiter:junit-jupiter-params")
}
