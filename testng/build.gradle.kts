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

        create("ant") {
            api(projects.testngAnt)
        }
        create("guice") {
            api(platform("com.google.inject:guice-bom:_"))
            api("com.google.inject:guice")
        }
        create("junit") {
            implementation(projects.testngRunnerJunit4)
        }
        create("yaml") {
            implementation("org.yaml:snakeyaml:_")
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
            "Bundle-Name" to name,
            "Bundle-SymbolicName" to project.group,
            "Bundle-Vendor" to vendor,
            // See http://docs.osgi.org/specification/osgi.core/7.0.0/framework.module.html#i2654895
            "Bundle-License" to "Apache-2.0",
            "Bundle-Description" to project.description,
            "Bundle-Version" to project.version,
            "Import-Package" to """
                bsh.*;version="[2.0.0,3.0.0)";resolution:=optional,
                com.beust.jcommander.*;version="[1.7.0,3.0.0)";resolution:=optional,
                com.google.inject.*;version="[1.2,1.3)";resolution:=optional,
                junit.framework;version="[3.8.1, 5.0.0)";resolution:=optional,
                org.junit.*;resolution:=optional,
                org.apache.tools.ant.*;version="[1.7.0, 2.0.0)";resolution:=optional,
                org.yaml.*;version="[1.6,2.0)";resolution:=optional,
                !com.beust.testng,
                !org.testng.*,
                !com.sun.*,
                *;resolution:=optional
            """.trimIndent().replace("\n", "")
        )
    }
}
