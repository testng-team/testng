import java.util.Properties
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

// The build JDK is defined once in the repository-root gradle.properties (single source of truth).
// Included builds do not inherit the root gradle.properties, so we read it from disk here.
val rootGradleProperties = Properties().apply {
    val f = rootDir.resolveSibling("gradle.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val buildJdkVersion = (rootGradleProperties.getProperty("jdkBuildVersion") ?: "25").toInt()

// We need a version supported by the current JVM and by the Kotlin Gradle plugin: use our build JDK,
// or fall back to 17 when running on an older JVM. Note this is the JVM target of the build scripts
// themselves, which is unrelated to TestNG's own targetJavaVersion.
listOf(buildJdkVersion, 17)
    .firstOrNull { JavaVersion.toVersion(it) <= JavaVersion.current() }
    ?.let { buildScriptJvmTarget ->
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(buildScriptJvmTarget))
            }
        }
    }
