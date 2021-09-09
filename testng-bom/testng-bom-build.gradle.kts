plugins {
    // Note: un-comment a dependency in testng.published-java-library when testng-bom becomes published
    id("testng.java-platform")
}

// Add a convenience pom.xml that sets all the versions
dependencies {
    constraints {
        api(projects.testngAnt)
        api(projects.testngApi)
        api(projects.testngAsserts)
        api(projects.testngCollections)
        api(projects.testngCoreApi)
        api(projects.testngCore)
        api(projects.testngReflectionUtils)
        api(projects.testngRunnerApi)
        api(projects.testngRunnerJunit4)
    }
}
