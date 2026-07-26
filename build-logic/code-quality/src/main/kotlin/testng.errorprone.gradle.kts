import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("net.ltgt.errorprone")
    id("build-logic.build-params")
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone.disableWarningsInGeneratedCode.set(true)
    // SelfAssertion only fires on TestNG's own sample/fixture classes, where trivial assertions
    // such as assertThat("abc").isEqualTo("abc") exist solely to give the runner a passing method.
    // Production code keeps the check enabled.
    if (name.contains("Test")) {
        options.errorprone.disable("SelfAssertion")
    }
}
