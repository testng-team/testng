dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"

include(":basics")
include(":build-parameters")
include(":code-quality")
include(":jvm")
include(":publishing")
