plugins {
    id("testng.java-platform")
}

// This is a convenience artifact to add dependencies to all the API jars
javaPlatform.allowDependencies()

dependencies {
    api("org.testng:testng-asserts:1.0.0")
    api(projects.testngCoreApi)
}
