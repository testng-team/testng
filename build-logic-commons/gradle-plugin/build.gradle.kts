import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion

plugins {
    `kotlin-dsl`
}

group = "org.testng.build-logic"

dependencies {
    // We use precompiled script plugins (== plugins written as src/kotlin/build-logic.*.gradle.kts files,
    // and we need to declare dependency on org.gradle.kotlin.kotlin-dsl:org.gradle.kotlin.kotlin-dsl.gradle.plugin
    // to make it work.
    // See https://github.com/gradle/gradle/issues/17016 regarding expectedKotlinDslPluginsVersion
    implementation("org.gradle.kotlin.kotlin-dsl:org.gradle.kotlin.kotlin-dsl.gradle.plugin:$expectedKotlinDslPluginsVersion")
}

// We need to figure out a version that is supported by the current JVM, and by the Kotlin Gradle plugin
// We use Java 21 (our build JDK) or fall back to 11 (our target compatibility) if running on an older JVM
listOf(21, 11)
    .firstOrNull { JavaVersion.toVersion(it) <= JavaVersion.current() }
    ?.let { buildScriptJvmTarget ->
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(buildScriptJvmTarget))
            }
        }
    }
