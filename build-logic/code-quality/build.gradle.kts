plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.buildParameters)
    api(projects.basics)
    api("org.sonarqube:org.sonarqube.gradle.plugin:7.3.1.8318")
    api("com.github.autostyle:autostyle-plugin-gradle:4.0.1")
    api("net.ltgt.gradle:gradle-errorprone-plugin:5.1.0")
}
