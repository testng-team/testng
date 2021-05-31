plugins {
    id("testng.published-java-library")
}

dependencies {
    api(projects.testngRunnerApi)
    api("junit:junit:_")
}
