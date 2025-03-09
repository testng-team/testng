plugins {
    id("testng.published-java-library")
    id("testng.merge-feature-jars")
}

description = "Testing framework for Java"

java {
    // The features would be merged into testng.jar as follows:
    // org.testng dependencies would be included to the jar
    // third-party dependencies would be left as regular dependencies
    optionalFeatures {
        shadedDependenciesFilter.set {
            it.owner.let { id -> id is ProjectComponentIdentifier && id.build.isCurrentBuild }
        }

        create("guice") {
            api(platform("com.google.inject:guice-bom:5.1.0"))
            api("com.google.inject:guice")
        }
        create("yaml") {
            implementation("org.yaml:snakeyaml:2.2")
        }
    }
}

dependencies {
    // Note: it is enough to mention key projects here, and testng transitives
    // would be selected automatically
    shadedDependencyElements(projects.testngAsserts)
    shadedDependencyElements(projects.testngCore)
}

tasks.mergedJar {
    manifest {
        // providers.gradleProperty does not work
        // see https://github.com/gradle/gradle/issues/14972
        val name = rootProject.findProperty("project.name")
        val vendor = rootProject.findProperty("project.vendor.name")
        attributes(
            // Java 9 module name
            "Automatic-Module-Name" to project.group,

            // BND Plugin instructions (for OSGi)
            "Bundle-ManifestVersion" to 2,
            "Bundle-Name" to name,
            "Bundle-SymbolicName" to project.group,
            "Bundle-Vendor" to vendor,
            // See http://docs.osgi.org/specification/osgi.core/7.0.0/framework.module.html#i2654895
            "Bundle-License" to "Apache-2.0",
            "Bundle-Description" to project.description,
            "Bundle-Version" to project.version.toString().removeSuffix("-SNAPSHOT"),
            //TestNG loads classes "by name" from configuration files, this allows to load such classes without need to know the exact package name
            "DynamicImport-Package" to "*",
            "Import-Package" to """
                com.beust.jcommander;version="1.83",
                org.slf4j;version="2.0",
                com.google.inject;version="1.4";resolution:=optional,
                org.yaml.snakeyaml;version="2.0";resolution:=optional,
                org.yaml.snakeyaml.nodes;version="2.0";resolution:=optional,
                org.yaml.snakeyaml.constructor;version="2.0";resolution:=optional
            """.trimIndent().replace("\n", ""),
            "Export-Package" to """
                org.testng
                org.testng.annotations
                org.testng.asserts
                org.testng.collections
                org.testng.internal
                org.testng.internal.annotations
                org.testng.internal.collections
                org.testng.internal.invokers
                org.testng.internal.invokers.objects
                org.testng.internal.objects
                org.testng.internal.objects.pojo
                org.testng.internal.reflect
                org.testng.internal.thread
                org.testng.internal.thread.graph
                org.testng.log
                org.testng.log4testng
                org.testng.reporters
                org.testng.reporters.jq
                org.testng.reporters.util
                org.testng.thread
                org.testng.util
                org.testng.xml
                org.testng.xml.internal
            """.trimIndent().replace("\n", ",")
        )
    }
}
