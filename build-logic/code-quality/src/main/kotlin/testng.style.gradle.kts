plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        importOrder()
        // Use the JavaParser-based engine: the default (google-java-format) engine throws
        // NoClassDefFoundError com.google.common.collect.ImmutableList in this build's classloader.
        removeUnusedImports("cleanthat-javaparser-unnecessaryimport")
        trimTrailingWhitespace()
        endWithNewline()
        googleJavaFormat()
    }
}
