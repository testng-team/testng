plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.buildParameters)
    api(projects.basics)
    api("org.sonarqube:org.sonarqube.gradle.plugin:4.4.1.3373")
    api("com.github.autostyle:autostyle-plugin-gradle:4.0")
}
