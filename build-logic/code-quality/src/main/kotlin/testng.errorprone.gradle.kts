import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("net.ltgt.errorprone")
    id("build-logic.build-params")
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.20.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.disableWarningsInGeneratedCode.set(true)
}
