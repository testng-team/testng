import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarqube:org.sonarqube.gradle.plugin:4.4.1.3373")
    implementation("com.github.autostyle:autostyle-plugin-gradle:4.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}
