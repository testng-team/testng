import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("testng.kotlin-library")
}

description = "Test fixtures shared by the TestNG modules. Never published."

// Everything here is test code even though it lives in a main source set. testng.errorprone
// reads this flag, and so does Error Prone itself -- several of its checks skip a compile
// marked test-only -- so opting a module in is a coverage decision, not just a switch.
//
// Error Prone is optional: testng.java only applies it when -PskipErrorProne is off, and the
// OpenRewrite job turns it off because Error Prone is a javac plugin OpenRewrite never sees.
// Reaching for options.errorprone unconditionally fails task configuration in those builds, so
// the option is registered only once the plugin that owns the extension is there.
pluginManager.withPlugin("net.ltgt.errorprone") {
    tasks.withType<JavaCompile>().configureEach {
        options.errorprone.compilingTestOnlyCode.set(true)
    }
}

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
