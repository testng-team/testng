

object This {
    val version = "7.0.1"
    val artifactId = "testng"
    val groupId = "org.testng"
    val description = "Testing framework for Java"
    val url = "https://testng.org"
    val scm = "github.com/cbeust/testng"

    // Should not need to change anything below
    val issueManagementUrl = "https://$scm/issues"
}

allprojects {
    group = This.groupId
    version = This.version
    apply<MavenPublishPlugin>()
    tasks.withType<Javadoc> {
        options {
            isFailOnError = false
            quiet()
            outputLevel = JavadocOutputLevel.QUIET
//            jFlags = listOf("-Xdoclint:none", "-quiet")
            "-quiet"
        }
    }
}

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2") }
    }
    dependencies {
        classpath("org.hibernate.build.gradle:version-injection-plugin:1.0.0")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://plugins.gradle.org/m2") }
}

plugins {
    java
    `java-library`
    `maven-publish`
    signing
    groovy
    id("org.sonarqube") version "2.7.1"
    id("com.jfrog.bintray") version "1.8.3" // Don't use 1.8.4, crash when publishing
}

dependencies {
    listOf("com.google.code.findbugs:jsr305:3.0.1",
            "org.apache.ant:ant:1.10.3",
            "junit:junit:4.12").forEach {
        compileOnly(it)
    }

    listOf("com.beust:jcommander:1.72", "com.google.inject:guice:4.1.0:no_aop",
            "org.yaml:snakeyaml:1.21").forEach {
        compile(it)
    }

    listOf("org.assertj:assertj-core:3.10.0",
            "org.codehaus.groovy:groovy-all:2.4.7",
            "org.spockframework:spock-core:1.0-groovy-2.4",
            "org.apache-extras.beanshell:bsh:2.0b6",
            "org.mockito:mockito-core:2.12.0",
            "org.jboss.shrinkwrap:shrinkwrap-api:1.2.6",
            "org.jboss.shrinkwrap:shrinkwrap-impl-base:1.2.6").forEach {
        testCompile(it)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Bundle-License" to "http://apache.org/licenses/LICENSE-2.0",
            "Bundle-Description" to "TestNG is a testing framework.",
            "Import-Package" to """
                "bsh.*;version="[2.0.0,3.0.0)";resolution:=optional",
                "com.beust.jcommander.*;version="[1.7.0,3.0.0)";resolution:=optional",
                "com.google.inject.*;version="[1.2,1.3)";resolution:=optional",
                "junit.framework;version="[3.8.1, 5.0.0)";resolution:=optional",
                "org.junit.*;resolution:=optional",
                "org.apache.tools.ant.*;version="[1.7.0, 2.0.0)";resolution:=optional",
                "org.yaml.*;version="[1.6,2.0)";resolution:=optional",
                "!com.beust.testng",
                "!org.testng.*",
                "!com.sun.*",
                "*"
            """,
            "Automatic-Module-Name" to "org.testng"
        )
    }
}

tasks.register<Copy>("filter") {
    from("src/main/resources/Version.java")
    into("src/main/java/org/testng/internal")
    expand("VERSION" to This.version)
}

tasks["compileJava"].dependsOn("filter")

tasks.test {
    useTestNG() {
        suiteXmlFiles.add(File("src/test/resources/testng.xml"))
        listeners.add("org.testng.reporters.FailedInformationOnConsoleReporter")
        testLogging.showStandardStreams = true
        systemProperties = mapOf("test.resources.dir" to "build/resources/test")
        maxHeapSize = "1500m"
    }
}

tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io/")
        property("sonar.github.repository", "cbeust/testng")
        property("sonar.github.login", "testng-bot")
    }
}

//
// Releases:
// ./gradlew bintrayUpload (to JCenter)
// ./gradlew publish (to Sonatype, then go to https://oss.sonatype.org/index.html#stagingRepositories to publish)
//

bintray {
    user = project.findProperty("bintrayUser")?.toString()
    key = project.findProperty("bintrayApiKey")?.toString()
    dryRun = false
    publish = false

    setPublications("custom")

    with(pkg) {
        repo = "maven"
        name = This.artifactId
        with(version) {
            name = This.version
            desc = This.description
            with(gpg) {
                sign = true
            }
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

with(publishing) {
    publications {
        create<MavenPublication>("custom") {
            groupId = This.groupId
            artifactId = This.artifactId
            version = project.version.toString()
            afterEvaluate {
                from(components["java"])
            }
            artifact(sourcesJar)
            artifact(javadocJar)
            pom {
                name.set(This.artifactId)
                description.set(This.description)
                url.set(This.url)
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set(This.issueManagementUrl)
                }
                developers {
                    developer {
                        id.set("cbeust")
                        name.set("Cedric Beust")
                        email.set("cedric@beust.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://${This.scm}.git")
                    url.set("https://${This.scm}")
                }
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            name = "sonatype"
            url = if (This.version.contains("SNAPSHOT"))
                uri("https://oss.sonatype.org/content/repositories/snapshots/") else
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("sonatypeUser") as? String
                password = project.findProperty("sonatypePassword") as? String
            }
        }
        maven {
            name = "myRepo"
            url = uri("file://$buildDir/repo")
        }
    }
}

with(signing) {
    sign(publishing.publications.getByName("custom"))
}
