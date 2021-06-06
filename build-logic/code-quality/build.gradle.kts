plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarqube:org.sonarqube.gradle.plugin:2.8")
    implementation("com.github.autostyle:autostyle-plugin-gradle:3.1")
}
