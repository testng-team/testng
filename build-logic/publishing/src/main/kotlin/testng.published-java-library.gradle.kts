import com.github.vlsi.gradle.publishing.dsl.simplifyXml
import com.github.vlsi.gradle.publishing.dsl.versionFromResolution

plugins {
    id("testng.reproducible-builds")
    id("testng.java-library")
    id("testng.maven-publish")
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    // If the user adds core and api with different versions,
    // then Gradle would select **both** core and api with the same version
    // Note: un-comment when testng-bom is published
    // implementation(platform(project(":testng-bom")))
    // For some reason this can't be in code-quality/testng.testing :(
    testImplementation(project(":testng-test-kit"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            // Gradle feature variants can't be mapped to Maven's pom
            suppressAllPomMetadataWarnings()

            // Use the resolved versions in pom.xml
            // Gradle might have different resolution rules, so we set the versions
            // that were used in Gradle build/test.
            versionFromResolution()

            pom {
                simplifyXml()
            }
        }
    }
}
