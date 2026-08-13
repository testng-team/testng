plugins {
    id("testng.java-library")
}

description = "JCommander based command line front end for TestNG"

dependencies {
    api(projects.testngCli)
    api("org.jcommander:jcommander:2.0")
    // Converter reads and writes YAML suites, but a command line front end must keep working
    // without the optional feature on the classpath, so the dependency stays compile only.
    compileOnly(projects.testngYaml)
    testImplementation(projects.testngTestKit)
}
