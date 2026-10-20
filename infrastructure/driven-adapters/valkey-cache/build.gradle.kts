dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    // Valkey speaks the Redis protocol: Spring Data Redis + Lettuce talk to it unchanged.
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("io.micrometer:micrometer-core")

    testImplementation(testFixtures(project(":application")))
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
}
