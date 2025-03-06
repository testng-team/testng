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

    testImplementation(projects.testng) {
        capabilities {
            requireFeature("guice")
        }
    }
    testImplementation(projects.testng) {
        capabilities {
            requireFeature("yaml")
        }
    }

    testImplementation("javax:javaee-api:8.0.1") {
        // The dependency was not obvious, logged https://github.com/cbeust/testng/issues/2578
        because("PaxExam uses javax.transaction.NotSupportedException and other classes")
    }
    testImplementation("org.ops4j.pax.exam:pax-exam-container-native:4.14.0")
    // pax-exam is not yet compatible with junit5
    // see https://github.com/ops4j/org.ops4j.pax.exam2/issues/886
    testImplementation("org.ops4j.pax.exam:pax-exam-testng:4.14.0")
    testImplementation("org.ops4j.pax.exam:pax-exam-link-mvn:4.14.0")
    testImplementation("org.ops4j.pax.url:pax-url-aether:2.6.12")
    testImplementation("org.apache.felix:org.apache.felix.framework:7.0.5")
    testRuntimeOnly("org.assertj:assertj-core:3.23.1")
    testRuntimeOnly("org.apache.servicemix.bundles:org.apache.servicemix.bundles.aopalliance:1.0_6") {
        because("Guice requires org.aopalliance.intercept package in osgi, however, aopalliance:aopalliance has no osgi headers")
    }
    testRuntimeOnly("com.google.errorprone:error_prone_annotations:2.36.0") {
        because("It is needed for Guava, only recent version of error_prone_annotations have osgi headers")
    }
    testRuntimeOnly("org.ops4j.pax.logging:pax-logging-api:2.2.8") {
        because("It will actually be used in osgi for logging")
    }
    testRuntimeOnly("org.ops4j.pax.logging:pax-logging-logback:2.2.8") {
        because("It will actually be used in osgi for logging, basic slf4j+logback is hard to launch in osgi, see https://stackoverflow.com/a/77867804")
    }
    testRuntimeOnly("org.apache.aries.spifly:org.apache.aries.spifly.dynamic.framework.extension:1.3.7") {
        because("slf4j-api 2.0 requires osgi.serviceloader.processor, see https://stackoverflow.com/a/77867804")
    }
}

// <editor-fold defaultstate="collapsed" desc="Pass dependency versions to pax-exam container">
val depDir = layout.buildDirectory.dir("pax-dependencies")

val generateDependenciesProperties by tasks.registering(WriteProperties::class) {
    description = "Generates dependencies.properties so pax-exam can use .versionAsInProject()"
    destinationFile.set(depDir.map { it.file("META-INF/maven/dependencies.properties") })
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
            property("$prefix/type", it.extension.orEmpty())
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
