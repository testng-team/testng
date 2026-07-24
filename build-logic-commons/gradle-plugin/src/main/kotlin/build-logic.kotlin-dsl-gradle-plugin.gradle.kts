import java.util.Properties

plugins {
    id("java-library")
    id("org.gradle.kotlin.kotlin-dsl") // this is 'kotlin-dsl' without version
}

tasks.validatePlugins {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}

// Java versions are defined once in the repository-root gradle.properties (single source of truth).
// Included builds do not inherit the root gradle.properties, so we read it from disk here.
val rootGradleProperties = Properties().apply {
    val f = rootDir.resolveSibling("gradle.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val buildJdkVersion = (rootGradleProperties.getProperty("jdkBuildVersion") ?: "21").toInt()
val targetJavaVersion = (rootGradleProperties.getProperty("targetJavaVersion") ?: "17").toInt()

// We need a version supported by the current JVM and by the Kotlin Gradle plugin:
// use our build JDK, or fall back to the target version when running on an older JVM.
listOf(buildJdkVersion, targetJavaVersion)
    .firstOrNull { JavaVersion.toVersion(it) <= JavaVersion.current() }
    ?.let { buildScriptJvmTarget ->
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(buildScriptJvmTarget))
            }
        }
    }
