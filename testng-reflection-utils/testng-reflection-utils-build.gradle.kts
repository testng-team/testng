plugins {
    id("testng.java-library")
}

dependencies {
    implementation(projects.testngCollections)
    implementation("org.checkerframework:checker-qual:3.36.0")
}
