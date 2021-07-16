plugins {
    id("testng.java-library")
}

dependencies {
    api("com.beust:jcommander:_")

    implementation(projects.testngCore)
    testImplementation(projects.testngAsserts)
}
