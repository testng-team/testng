plugins {
    id("org.gradlex.build-parameters") version "1.4.3"
}

buildParameters {
    pluginId("testng.build-parameters")
    bool("enableCheckerframework") {
        defaultValue.set(false)
        description.set("Run CheckerFramework (nullness) verifications")
    }
    bool("enableErrorprone") {
        defaultValue.set(true)
        description.set("Enable ErrorProne verifications")
    }
}
