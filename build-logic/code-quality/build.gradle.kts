import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarqube:org.sonarqube.gradle.plugin:4.3.0.3225")
    implementation("com.github.autostyle:autostyle-plugin-gradle:3.2")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}
