plugins {
    id("testng.java-library")
}

description = "JCommander based command line front end for TestNG"

dependencies {
    api(projects.testngCli)
    api("org.jcommander:jcommander:2.0")
    // org.testng.internal.Yaml, used by Converter, exposes snakeyaml on its API
    compileOnly("org.yaml:snakeyaml:2.2")
    testImplementation(projects.testngTestKit)
}
