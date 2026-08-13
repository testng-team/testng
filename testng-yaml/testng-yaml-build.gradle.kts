plugins {
    id("testng.java-library")
}

description = "YAML suite file support for TestNG"

dependencies {
    api(projects.testngCore)
    // Also what the round trip tests use to load the emitted YAML with a plain parser, so that the
    // assertion does not depend on TestNG's own binding: testImplementation extends implementation.
    implementation("org.yaml:snakeyaml:2.6")

    testImplementation(projects.testngTestKit)
    // A binding, so that the deprecation warning the reader emits for an old key spelling is
    // observable at all: without one slf4j discards it and the assertion would pass on silence.
    testImplementation("org.slf4j:slf4j-simple:2.0.18")
}

tasks.test {
    (testFramework.options as TestNGOptions).suites("src/test/resources/testng.xml")
}
