rootProject.name = "spring-boot-hexagonal-architecture"

include(":domain")
project(":domain").projectDir = file("domain")

include(":application")
project(":application").projectDir = file("application")

// Entry points (inbound adapters)
include(":rest-controller")
project(":rest-controller").projectDir = file("infrastructure/entry-points/rest-controller")

include(":scheduler")
project(":scheduler").projectDir = file("infrastructure/entry-points/scheduler")

// Driven adapters (outbound adapters)
include(":jdbc-repository")
project(":jdbc-repository").projectDir = file("infrastructure/driven-adapters/jdbc-repository")

include(":valkey-cache")
project(":valkey-cache").projectDir = file("infrastructure/driven-adapters/valkey-cache")

include(":metrics-publisher")
project(":metrics-publisher").projectDir = file("infrastructure/driven-adapters/metrics-publisher")

include(":bootstrap")
project(":bootstrap").projectDir = file("bootstrap")
