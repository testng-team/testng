plugins {
    `java-base`
    id("testng.versioning")
    id("testng.style")
    id("testng.repositories")
    // Improves Gradle Test logging
    // See https://github.com/vlsi/vlsi-release-plugins/tree/master/plugins/gradle-extensions-plugin
    id("com.github.vlsi.gradle-extensions")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
