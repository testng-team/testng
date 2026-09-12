import buildlogic.VerifyTestExecution

plugins {
    `java-library`
}

// Wired into check, so `./gradlew build` -- what CI runs -- proves the suite executed what it says
// it executes. See VerifyTestExecution for why the build has to check this rather than trust it.
val verifyTestExecution = tasks.register<VerifyTestExecution>("verifyTestExecution") {
    description = "Verifies the suite ran every class it names, ran no samples, and did not change"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    dependsOn(tasks.test)
    suite.convention(layout.projectDirectory.file("src/test/resources/testng.xml"))
    inventory.convention(layout.projectDirectory.file("execution-inventory.txt"))
    knownSilent.convention(layout.projectDirectory.file("execution-known-silent.txt"))
    factoryProduced.convention(layout.projectDirectory.file("execution-factory-produced.txt"))
    results.convention(layout.buildDirectory.dir("test-results/test"))
    update.convention(providers.gradleProperty("updateExecutionInventory").map { true }.orElse(false))
}

tasks.check {
    dependsOn(verifyTestExecution)
    // The rules VerifyTestExecution applies have their own tests, because a wrong answer there is
    // silent both ways: a real defect waved through, or a green branch turned red for nothing.
    // An included build runs no task on its own, so check has to ask, or CI would never run them.
    dependsOn(gradle.includedBuild("build-logic").task(":code-quality:test"))
}
