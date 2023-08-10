plugins {
    `java-base`
    id("testng.versioning")
    id("testng.style")
    // id("testng.build-parameters") // Plugin [id: 'testng.build-parameters'] was not found
    id("testng.repositories")
    // Improves Gradle Test logging
    // See https://github.com/vlsi/vlsi-release-plugins/tree/master/plugins/gradle-extensions-plugin
    id("com.github.vlsi.gradle-extensions")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

if (true /*buildParameters.enableCheckerframework*/) {
    apply(plugin = "testng.checkerframework")
}
if (true /*buildParameters.enableErrorprone*/) {
    apply(plugin = "testng.errorprone")
}

tasks.withType<JavaCompile>().configureEach {
    inputs.property("java.version", System.getProperty("java.version"))
    inputs.property("java.vendor", System.getProperty("java.vendor"))
    inputs.property("java.vm.version", System.getProperty("java.vm.version"))
    inputs.property("java.vm.vendor", System.getProperty("java.vm.vendor"))
}

tasks.withType<Test>().configureEach {
    inputs.property("java.version", System.getProperty("java.version"))
    inputs.property("java.vendor", System.getProperty("java.vendor"))
    inputs.property("java.vm.version", System.getProperty("java.vm.version"))
    inputs.property("java.vm.vendor", System.getProperty("java.vm.vendor"))
}
