import buildlogic.registerOptionalFeatureVariants

plugins {
    id("testng.java-library")
}

registerOptionalFeatureVariants("guice", buildParameters.targetJavaVersion, tasks.jar)

dependencies {
    api(projects.testngCollections)
    compileOnly("com.github.spotbugs:spotbugs:4.10.3")
    "guiceApi"(platform("com.google.inject:guice-bom:5.1.0"))
    "guiceApi"("com.google.inject:guice")

    implementation(projects.testngReflectionUtils)
    api("org.slf4j:slf4j-api:2.0.18")
    testImplementation("org.slf4j:slf4j-simple:2.0.18")
}
