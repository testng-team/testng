dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"

include(":basics")
include(":code-quality")
include(":jvm")
include(":publishing")
