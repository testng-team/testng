pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "testng-root"

plugins {
    `gradle-enterprise`
    id("de.fayard.refreshVersions") version "0.60.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

// Sorted by name
include(":testng")
include(":testng-ant")
include(":testng-api")
include(":testng-asserts")
include(":testng-bom")
include(":testng-collections")
include(":testng-core")
include(":testng-core-api")
include(":testng-reflection-utils")
include(":testng-runner-api")
include(":testng-test-kit")
include(":testng-test-osgi")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

for (project in rootProject.children) {
    project.apply {
        buildFileName = "${project.name}-build.gradle.kts"
    }
}
