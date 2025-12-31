import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("testng.java-library")
    kotlin("jvm")
}

dependencies {
    testImplementation(platform("org.jetbrains.kotlin:kotlin-bom:2.3.0"))
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
        val jdkRelease = buildParameters.targetJavaVersion.toString()
        freeCompilerArgs.add("-Xjdk-release=$jdkRelease")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(jdkRelease))
    }
}
