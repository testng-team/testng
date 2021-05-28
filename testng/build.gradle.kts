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
