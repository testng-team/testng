import buildlogic.filterEolSimple

plugins {
    `java-library`
    id("testng.java")
    id("testng.testing")
}

tasks.withType<Javadoc>().configureEach {
    excludes.add("org/testng/internal/**")
}

tasks.withType<JavaCompile>().configureEach {
    inputs.property("java.version", System.getProperty("java.version"))
    inputs.property("java.vm.version", System.getProperty("java.vm.version"))
    options.apply {
        encoding = "UTF-8"
        compilerArgs.add("-Xlint:deprecation")
        compilerArgs.add("-Werror")
    }
}

tasks.withType<Jar>().configureEach {
    into("META-INF") {
        filterEolSimple("crlf")
        from("$rootDir/LICENSE.txt")
        from("$rootDir/NOTICE")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    manifest {
        // providers.gradleProperty does not work
        // see https://github.com/gradle/gradle/issues/14972
        val name = rootProject.findProperty("project.name")
        val vendor = rootProject.findProperty("project.vendor.name")
        attributes(mapOf(
            "Specification-Title" to name,
            "Specification-Version" to project.version,
            "Specification-Vendor" to vendor,
            "Implementation-Title" to name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to vendor,
            "Implementation-Vendor-Id" to rootProject.findProperty("project.vendor.id"),
            "Implementation-Url" to rootProject.findProperty("project.url"),
        ))
    }
}

@Suppress("unused")
val transitiveSourcesElements by configurations.creating {
    description = "Share sources folder with other projects for aggregation (e.g. sources, javadocs, etc)"
    isVisible = false
    isCanBeResolved = false
    isCanBeConsumed = true
    extendsFrom(configurations.implementation.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("source-folders"))
    }
    // afterEvaluate is to allow creation of the new source sets
    afterEvaluate {
        sourceSets.main.get().java.srcDirs.forEach { outgoing.artifact(it) }
    }
}
