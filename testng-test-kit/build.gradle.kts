plugins {
    id("testng.java-library")
}

dependencies {
    api(projects.testngCoreApi)
    implementation(projects.testngAsserts)
    implementation(projects.testngCore)
}
