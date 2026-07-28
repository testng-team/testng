pluginManagement {
    // Gradle 9 resolves plugins from included builds only when they are included
    // via pluginManagement. build-logic-commons provides build-logic.kotlin-dsl-gradle-plugin.
    includeBuild("../build-logic-commons")
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":basics")
include(":build-parameters")
include(":code-quality")
include(":jvm")
include(":publishing")
