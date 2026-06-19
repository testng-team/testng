plugins {
    id("testng.java-library")
}

dependencies {
    api("org.assertj:assertj-core:3.27.7") {
        because("org.testng.Assert delegates to AssertJ (GITHUB-2649)")
    }
    implementation(projects.testngCollections) {
        because("Lists.newArrayList")
    }

    testImplementation("org.testng:testng:7.3.0") {
        because("core depends on assertions and we need testng to test assertions")
    }
    testImplementation(projects.testngTestKit)
}
