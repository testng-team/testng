plugins {
    id("testng.java-library")
}

dependencies {
    api(projects.testngCoreApi)
    compileOnly("com.github.spotbugs:spotbugs:4.10.3")
}
