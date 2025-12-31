plugins {
    `maven-publish`
    id("testng.local-maven-repo")
    id("testng.signing")
    id("build-logic.build-params")
}

// It takes value from root project always: https://github.com/gradle/gradle/issues/13302
val scmUrl = providers.gradleProperty("scm.url")

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            pom {
                name.set(artifactId)
                description.set(providers.provider { project.description })
                // It takes value from root project always: https://github.com/gradle/gradle/issues/13302
                url.set(providers.gradleProperty("project.url"))
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set(scmUrl.map { "${it.removeSuffix(".git")}/issues" })
                }
                developers {
                    developer {
                        id.set("cbeust")
                        name.set("Cedric Beust")
                        email.set("cedric@beust.com")
                    }
                    developer {
                        id.set("juherr")
                        name.set("Julien Herr")
                        email.set("julien@herr.fr")
                    }
                    developer {
                        id.set("krmahadevan")
                        name.set("Krishnan Mahadevan")
                        email.set("krishnan.mahadevan1978@gmail.com")
                    }
                }
                scm {
                    connection.set(scmUrl.map { "scm:git:$it" })
                    url.set(scmUrl)
                }
            }
        }
    }
}

// Configure Maven Central Portal publishing via nmcp plugin
// The nmcp plugin is applied in the root build.gradle.kts and configured there
// Individual projects just need to have publications configured via maven-publish plugin
// For snapshot versions, configure Central Snapshots repository
val isRelease = providers.gradleProperty("release").map { it.toBoolean() }.orElse(false)
if (!isRelease.get()) {
    publishing {
        repositories {
            maven {
                name = "centralSnapshots"
                url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                credentials {
                    username = providers.environmentVariable("CENTRAL_PORTAL_USERNAME").orNull
                    password = providers.environmentVariable("CENTRAL_PORTAL_PASSWORD").orNull
                }
            }
        }
    }
}
