plugins {
    id("testng.java-library")
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
