import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(project(":jvm"))
    implementation("com.github.vlsi.gradle-extensions:com.github.vlsi.gradle-extensions.gradle.plugin:1.90")
    implementation("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:8.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
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
