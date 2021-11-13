import org.gradle.api.tasks.testing.Test

plugins {
    `java-library`
}

dependencies {
    testImplementation("org.assertj:assertj-core:_")
}

tasks.withType<Test>().configureEach {
    useTestNG()
    providers.gradleProperty("testng.test.extra.jvmargs")
        .forUseAtConfigurationTime()
        .orNull?.toString()?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let {
            // TODO: support quoted arguments
            jvmArgs(it.split(Regex("\\s+")))
        }
    systemProperty("test.resources.dir", "build/resources/test")
    fun passProperty(name: String, default: String? = null) {
        val value = System.getProperty(name) ?: default
        value?.let { systemProperty(name, it) }
    }
    // Default verbose is 0, however, it can be adjusted vi -Dtestng.default.verbose=2
    passProperty("testng.default.verbose", "0")
    // Allow running tests in a custom locale with -Duser.language=...
    passProperty("user.language")
    passProperty("user.country")

    @Suppress("unchecked_cast")
    val props = System.getProperties().propertyNames() as `java.util`.Enumeration<String>
    // Pass testng.* properties to the test JVM
    for (e in props) {
        if (e.startsWith("testng.")) {
            passProperty(e)
        }
    }
}
