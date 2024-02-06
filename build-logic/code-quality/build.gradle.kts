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

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(project.property("jdkBuildVersion") as String))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set((project.property("targetJavaVersion") as String).toInt())
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = project.property("targetJavaVersion") as String
    }
}
