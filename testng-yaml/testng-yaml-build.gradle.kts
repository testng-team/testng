plugins {
    id("testng.java-library")
}

description = "YAML suite file support for TestNG"

dependencies {
    api(projects.testngCore)
    implementation("org.yaml:snakeyaml:2.6")

    testImplementation(projects.testngTestKit)
    // The round trip tests load what the writer emits with a plain snakeyaml parser, so that the
    // assertion does not depend on TestNG's own binding.
    testImplementation("org.yaml:snakeyaml:2.6")
}

tasks.test {
    (testFramework.options as TestNGOptions).suites("src/test/resources/testng.xml")
}
