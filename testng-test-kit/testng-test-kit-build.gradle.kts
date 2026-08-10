plugins {
    id("testng.kotlin-library")
}

description = "Test fixtures shared by the TestNG modules. Never published."

dependencies {
    api(platform("org.jetbrains.kotlin:kotlin-bom:2.4.10"))
    api("org.jetbrains.kotlin:kotlin-stdlib") {
        because("SimpleBaseTest is a Kotlin class extended from Java-only modules, so the stdlib is part of this module's ABI, not an implementation detail")
    }

    // compileOnly on purpose: this module is only ever put on a test classpath that already
    // carries TestNG, and an api edge here would drag testng-core into modules that deliberately
    // resolve a released org.testng artifact instead.
    compileOnly(projects.testngCore)

    api("org.assertj:assertj-core:3.27.7") {
        because("SimpleBaseTest exposes assertions")
    }
    api("org.jboss.shrinkwrap:shrinkwrap-api:1.2.6") {
        because("JarCreator builds test jars")
    }
    runtimeOnly("org.jboss.shrinkwrap:shrinkwrap-impl-base:1.2.6")
}
