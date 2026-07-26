plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.buildParameters)
    api(projects.basics)
    api(projects.codeQuality)
    api("com.github.vlsi.gradle-extensions:com.github.vlsi.gradle-extensions.gradle.plugin:3.0.2")
    api("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.4.10")
}
