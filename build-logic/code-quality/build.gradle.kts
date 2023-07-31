import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.sonarqube:org.sonarqube.gradle.plugin:2.8")
    implementation("com.github.autostyle:autostyle-plugin-gradle:3.1")
    implementation("org.checkerframework:checkerframework-gradle-plugin:0.6.29")
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:3.1.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}
