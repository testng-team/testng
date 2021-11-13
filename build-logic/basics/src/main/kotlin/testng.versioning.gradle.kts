if (project != rootProject) {
    // The root project takes its version from /gradle.properties -> testng.version
    // All the rest projects take the version from the root
    version = rootProject.version
}
