plugins {
    // Note: un-comment a dependency in testng.published-java-library when testng-bom becomes published
    id("testng.java-platform")
}

// Add a convenience pom.xml that sets all the versions
dependencies {
    constraints {
        api(projects.testngApi)
        api("org.testng:testng-asserts:1.0.0")
        api(projects.testngCollections)
        api(projects.testngCoreApi)
        api(projects.testngCore)
        api(projects.testngReflectionUtils)
        api(projects.testngRunnerApi)
    }
}
