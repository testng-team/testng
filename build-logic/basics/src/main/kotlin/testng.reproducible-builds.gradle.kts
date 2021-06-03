tasks.withType<AbstractArchiveTask>().configureEach {
    // Ensure builds are reproducible
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    dirMode = "775".toInt(8)
    fileMode = "664".toInt(8)
}
