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
    // SelfAssertion (new in Error Prone 2.x) only flags intentional trivial assertions in
    // TestNG's sample/fixture test classes (e.g. assertThat("abc").isEqualTo("abc")), which exist
    // solely to give the runner a passing method. Keep it off to avoid false positives on fixtures.
    options.errorprone.disable("SelfAssertion")
}
