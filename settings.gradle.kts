pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "testng-root"

plugins {
    id("com.gradle.develocity") version("3.19.1")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        val isCI = System.getenv("CI") != null
        publishing.onlyIf { isCI }
    }
}

// Sorted by name
include(":testng")
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
