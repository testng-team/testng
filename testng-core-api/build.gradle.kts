plugins {
    id("testng.published-java-library")
}

dependencies {
    api(projects.testngCollections)
    api("com.google.code.findbugs:jsr305:_")
    api(platform("com.google.inject:guice-bom:_"))
    api("com.google.inject:guice")
}
