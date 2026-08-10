plugins {
    id("testng.java-library")
}

description = "Command line contract and shared configuration logic for TestNG"

dependencies {
    api(projects.testngCore)
}
