plugins {
    // Published as a standalone, optional artifact (no longer merged into the testng fat jar).
    // See GITHUB-2649 and GITHUB-3197.
    id("testng.published-java-library")
}

description = "TestNG assertions (org.testng.Assert), backed by AssertJ"

// NOTE: this artifact is published as a plain JAR, NOT an OSGi bundle.
// org.testng.Assert and org.testng.FileAssert live in the org.testng package, which is also
// exported by the main testng bundle (core classes). Shipping testng-asserts as its own bundle that
// re-exports org.testng would create a split package, which OSGi (and the Java module system)
// handle poorly. Providing proper OSGi metadata therefore needs dedicated design and is tracked as a
// follow-up; for now OSGi consumers should migrate to AssertJ (see docs/MIGRATING_ASSERTIONS.md).
// See GITHUB-3197.

dependencies {
    api("org.assertj:assertj-core:3.27.7") {
        because("org.testng.Assert delegates to AssertJ (GITHUB-2649)")
    }

    testImplementation("org.testng:testng:7.3.0") {
        because("core depends on assertions and we need testng to test assertions")
    }
    testImplementation(projects.testngTestKit)
}
