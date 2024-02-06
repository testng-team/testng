import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(project(":basics"))
    implementation(project(":code-quality"))
    implementation("com.github.vlsi.gradle-extensions:com.github.vlsi.gradle-extensions.gradle.plugin:1.90")
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.6.21")
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
