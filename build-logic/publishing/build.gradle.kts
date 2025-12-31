plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.jvm)
    api("com.github.vlsi.gradle-extensions:com.github.vlsi.gradle-extensions.gradle.plugin:1.90")
    api("com.gradleup.nmcp:com.gradleup.nmcp.gradle.plugin:1.0.2")
    api("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:8.1.1")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin")
}
