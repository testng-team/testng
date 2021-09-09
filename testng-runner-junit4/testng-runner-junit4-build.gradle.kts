plugins {
    id("testng.java-library")
}

dependencies {
    api(projects.testngRunnerApi)
    api("junit:junit:_")
}
