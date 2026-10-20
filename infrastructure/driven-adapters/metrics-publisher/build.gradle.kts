dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation("org.springframework:spring-context")
    implementation("io.micrometer:micrometer-core")

    testImplementation(testFixtures(project(":application")))
}
