plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.jvm)
    api("com.github.vlsi.gradle-extensions:com.github.vlsi.gradle-extensions.gradle.plugin:4.0.0")
    api("com.gradleup.nmcp:com.gradleup.nmcp.gradle.plugin:1.6.2")
    api("com.gradleup.shadow:com.gradleup.shadow.gradle.plugin:9.6.1")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin")
}
