import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("org.springframework.boot") version "4.0.8" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.1.0" apply false
    id("org.sonarqube") version "7.2.3.7755"
}

allprojects {
    group = "my.packagename"
    version = property("version") as String

    repositories {
        mavenCentral()
        // Codehunters SDK (outbox). Restricted to its group so no other artifact can be served from it.
        maven {
            name = "CodehuntersSdk"
            url = uri(property("codehunters.sdk.repository.url") as String)
            credentials {
                username = System.getenv("GH_PACKAGES_USERNAME") ?: findProperty("GH_PACKAGES_USERNAME") as String?
                password = System.getenv("GH_PACKAGES_TOKEN") ?: findProperty("GH_PACKAGES_TOKEN") as String?
            }
            content { includeGroup("io.codehunters") }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")

    val javaVersion = property("java.version").toString().toInt()
    val minimumLineCoverage = property("jacoco.minimum.line.coverage").toString().toBigDecimal()
    val googleJavaFormatVersion = property("google.java.format.version").toString()

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(javaVersion)
        }
    }

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("spring.boot.version")}")
        }
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testImplementation"("org.assertj:assertj-core")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = javaVersion
        // -parameters lets Spring bind constructor/record parameter names without reflection hacks.
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all,-processing,-serial", "-Werror"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
        testLogging { events("passed", "skipped", "failed") }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target("src/**/*.java")
            googleJavaFormat(googleJavaFormatVersion)
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    val jacocoExclusions = listOf("**/config/**", "**/*Application*")

    fun JacocoReportBase.excludeNonLogicClasses() {
        classDirectories.setFrom(
            files(classDirectories.files.map { fileTree(it) { exclude(jacocoExclusions) } }),
        )
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.named("test"))
        excludeNonLogicClasses()
        reports {
            xml.required = true
            html.required = true
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(tasks.named("test"))
        excludeNonLogicClasses()
        violationRules {
            rule {
                limit {
                    counter = "LINE"
                    minimum = minimumLineCoverage
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn("jacocoTestCoverageVerification", "spotlessCheck")
    }
}

sonar {
    properties {
        property("sonar.projectKey", "spring-boot-hexagonal-architecture")
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "https://sonarcloud.io")
        property("sonar.token", System.getenv("SONAR_TOKEN") ?: "")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.gradle.skipCompile", "true")
        property("sonar.exclusions", "**/src/test/**")
        property("sonar.coverage.exclusions", "**/config/**,**/*Application*")
    }
}
