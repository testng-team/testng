plugins {
    id("testng.published-java-library")
}

java {
    registerFeature("guice") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    api(projects.testngCollections)
    api("com.google.code.findbugs:jsr305:_")
    "guiceApi"(platform("com.google.inject:guice-bom:_"))
    "guiceApi"("com.google.inject:guice")
}
