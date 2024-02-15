plugins {
    id("java-library")
    id("org.gradle.kotlin.kotlin-dsl") // this is 'kotlin-dsl' without version
}

tasks.validatePlugins {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}

// We need to figure out a version that is supported by the current JVM, and by the Kotlin Gradle plugin
// So we settle on 17 or 11 if the current JVM supports it
listOf(17, 11)
    .firstOrNull { JavaVersion.toVersion(it) <= JavaVersion.current() }
    ?.let { buildScriptJvmTarget ->
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(buildScriptJvmTarget))
            }
        }
    }
