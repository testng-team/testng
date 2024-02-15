dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("../build-logic-commons")
include(":basics")
include(":build-parameters")
include(":code-quality")
include(":jvm")
include(":publishing")
