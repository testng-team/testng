import org.checkerframework.gradle.plugin.CheckerFrameworkExtension

plugins {
    id("org.checkerframework")
}

dependencies {
    checkerFramework("org.checkerframework:checker:3.36.0")
}

configure<CheckerFrameworkExtension> {
    checkers = listOf(
        "org.checkerframework.checker.nullness.NullnessChecker",
        "org.checkerframework.checker.optional.OptionalChecker"
    )
}

tasks.withType<JavaCompile>().configureEach {
    // Don't run the checker on generated code.
    if (name.startsWith("compileMainGenerated")) {
        checkerFramework {
            skipCheckerFramework = true
        }
    }
}
