plugins {
    // Published as a standalone, optional artifact (no longer merged into the testng fat jar).
    // See GITHUB-2649 and GITHUB-3197.
    id("testng.published-java-library")
}

description = "TestNG assertions (org.testng.Assert), backed by AssertJ"

dependencies {
    api("org.assertj:assertj-core:3.27.7") {
        because("org.testng.Assert delegates to AssertJ (GITHUB-2649)")
    }

    testImplementation("org.testng:testng:7.3.0") {
        because("core depends on assertions and we need testng to test assertions")
    }
    testImplementation(projects.testngTestKit)
}
