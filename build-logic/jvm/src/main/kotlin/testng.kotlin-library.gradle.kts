import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("testng.java-library")
    kotlin("jvm")
}

dependencies {
    testImplementation(platform("org.jetbrains.kotlin:kotlin-bom:1.6.20"))
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-Xjvm-default=all"
        val jdkRelease = buildParameters.targetJavaVersion.toString()
        freeCompilerArgs += "-Xjdk-release=$jdkRelease"
        kotlinOptions.jvmTarget = jdkRelease
    }
}
