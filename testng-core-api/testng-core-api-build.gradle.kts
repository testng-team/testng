plugins {
    id("testng.java-library")
}

java {
    registerFeature("guice") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    api(projects.testngCollections)
    compileOnly("com.github.spotbugs:spotbugs:4.8.1")
    "guiceApi"(platform("com.google.inject:guice-bom:5.1.0"))
    "guiceApi"("com.google.inject:guice")

    implementation(projects.testngReflectionUtils)
    api("org.slf4j:slf4j-api:2.0.16")
    testImplementation("org.slf4j:slf4j-simple:2.0.16")
}
