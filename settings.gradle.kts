pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "testng-root"

plugins {
    `gradle-enterprise`
    id("de.fayard.refreshVersions") version "0.10.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
    }
}

include("core")

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
