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
}

tasks.test {
    (testFramework.options as TestNGOptions).suites("src/test/resources/testng.xml")
}
