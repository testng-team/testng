import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("testng.java-library")
}

// Error Prone is optional: testng.java only applies it when -PskipErrorProne is off, and the
// OpenRewrite job turns it off because Error Prone is a javac plugin OpenRewrite never sees.
// Reaching for options.errorprone unconditionally fails task configuration in those builds, so the
// opt-out is registered only once the plugin that owns the extension is there.
pluginManager.withPlugin("net.ltgt.errorprone") {
    tasks.withType<JavaCompile>().configureEach {
        // The deprecated factories are one-line wrappers over a JDK constructor, so
        // InlineMeSuggester fires on every one of them -- twenty warnings that say the same thing.
        // Acting on them means annotating with @InlineMe, which needs error_prone_annotations on
        // the compile classpath, and anything reachable from :testng-core lands in the published
        // pom, where verifyPublishedPomDependencies would reject it. The javadoc names the
        // replacement instead.
        options.errorprone.disable("InlineMeSuggester")
    }
}

dependencies {
    // The released artifact, not projects.testngCore. This module is the leaf of the project graph
    // -- everything else depends on it -- so a project edge to core would be a dependency cycle.
    // Gradle resolves this coordinate from Maven Central, so the task graph stays acyclic.
    //
    // The released jar also carries org.testng.collections, but a test classpath puts this module's
    // own classes first, so the tests always exercise the local sources. That also means the TestNG
    // runtime runs against the local collections: a signature change here breaks the test run with
    // a NoSuchMethodError, which is loud rather than silent.
    testImplementation("org.testng:testng:7.12.0")
}
