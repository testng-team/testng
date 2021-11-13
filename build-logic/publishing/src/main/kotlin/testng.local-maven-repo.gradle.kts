plugins {
    `maven-publish`
}

val localRepoElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    description =
        "Shares local maven repository directory that contains the artifacts produced by the current project"
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named("maven-repository"))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }
}

val localRepoDir = layout.buildDirectory.dir("local-maven-repo")

publishing {
    repositories {
        maven {
            name = "local"
            url = uri(localRepoDir)
        }
    }
}

localRepoElements.outgoing.artifact(localRepoDir) {
    builtBy(tasks.named("publishAllPublicationsToLocalRepository"))
}

val cleanLocalRepository by tasks.registering(Delete::class) {
    description = "Clears local-maven-repo so timestamp-based snapshot artifacts do not consume space"
    delete(localRepoDir)
}

tasks.withType<PublishToMavenRepository>()
    .matching { it.name.contains("ToLocalRepository") }
    .configureEach {
        dependsOn(cleanLocalRepository)
    }
