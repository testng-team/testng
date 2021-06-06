plugins {
    id("com.github.autostyle")
}

autostyle {
    java {
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        googleJavaFormat()
    }
}
