plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.buildParameters)
    api(projects.basics)
    api("org.sonarqube:org.sonarqube.gradle.plugin:4.4.1.3373")
    api("com.diffplug.spotless:spotless-plugin-gradle:8.8.0")
    api("net.ltgt.gradle:gradle-errorprone-plugin:4.1.0")
}
