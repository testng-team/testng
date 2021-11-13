plugins {
    id("testng.java-library")
}

val testngRepository by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    description =
        "Consumes local maven repository directory that contains the artifacts produced by :testng"
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named("maven-repository"))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }
}

dependencies {
    testngRepository(projects.testng)

    testImplementation(projects.testng)

    testImplementation("javax:javaee-api:_") {
        // The dependency was not obvious, logged https://github.com/cbeust/testng/issues/2578
        because("PaxExam uses javax.transaction.NotSupportedException and other classes")
    }
    testImplementation("org.ops4j.pax.exam:pax-exam-container-native:_")
    // pax-exam is not yet compatible with junit5
    // see https://github.com/ops4j/org.ops4j.pax.exam2/issues/886
    testImplementation("org.ops4j.pax.exam:pax-exam-testng:_")
    testImplementation("org.ops4j.pax.exam:pax-exam-link-mvn:_")
    testImplementation("org.ops4j.pax.url:pax-url-aether:_")
    testImplementation("org.apache.felix:org.apache.felix.framework:_")
    testImplementation("ch.qos.logback:logback-core:_")
    testImplementation("ch.qos.logback:logback-classic:_")
}

// <editor-fold defaultstate="collapsed" desc="Pass dependency versions to pax-exam container">
val depDir = layout.buildDirectory.dir("pax-dependencies")

val generateDependenciesProperties by tasks.registering(WriteProperties::class) {
    description = "Generates dependencies.properties so pax-exam can use .versionAsInProject()"
    setOutputFile(depDir.map { it.file("META-INF/maven/dependencies.properties") })
    property("groupId", project.group)
    property("artifactId", project.name)
    property("version", project.version)
    property("${project.group}/${project.name}/version", "${project.version}")
    dependsOn(configurations.testRuntimeClasspath)
    dependsOn(testngRepository)
    doFirst {
        configurations.testRuntimeClasspath.get().resolvedConfiguration.resolvedArtifacts.forEach {
            val prefix = "${it.moduleVersion.id.group}/${it.moduleVersion.id.name}"
            property("$prefix/scope", "compile")
            property("$prefix/type", it.extension)
            property("$prefix/version", it.moduleVersion.id.version)
        }
    }
}

sourceSets.test {
    output.dir(mapOf("builtBy" to generateDependenciesProperties), depDir)
}
// </editor-fold>

// This repository is used instead of ~/.m2/... to avoid clash with /.m2/ contents
val paxLocalCacheRepository = layout.buildDirectory.dir("pax-repo")

val cleanCachedTestng by tasks.registering(Delete::class) {
    description =
        "Removes cached testng.jar from pax-repo folder so pax-exam always resolves the recent one"
    delete(paxLocalCacheRepository.map { it.dir("org/testng") })
}

tasks.test {
    dependsOn(generateDependenciesProperties)
    dependsOn(cleanCachedTestng)
    dependsOn(testngRepository)
    systemProperty("logback.configurationFile", file("src/test/resources/logback-test.xml"))
    // Regular systemProperty can't be used here as we need lazy evaluation of testngRepository
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-Dtestng.org.ops4j.pax.url.mvn.repositories=" +
                    "file:${testngRepository.singleFile.absolutePath}@snapshots@id=testng-current" +
                    ",${project.findProperty("osgi.test.mavencentral.url")}@id=central",
            "-Dtestng.org.ops4j.pax.url.mvn.localRepository=file:${paxLocalCacheRepository.get().asFile.absolutePath}@id=pax-repo"
        )
    })
}
