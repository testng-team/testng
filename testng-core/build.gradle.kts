plugins {
    id("testng.java-library")
    groovy
    id("testng.sonarqube")
}

// TODO: remove
object This {
    const val version = "7.5.0-SNAPSHOT"
    const val artifactId = "testng"
    const val groupId = "org.testng"
    const val description = "Testing framework for Java"
    const val url = "https://testng.org"
    const val scm = "github.com/cbeust/testng"

    // Should not need to change anything below
    const val name = "TestNG"
    const val vendor = name
}

allprojects {
    group = This.groupId
    version = This.version
}

java {
    // use gradle feature
    // in order to optionally exposed transitive dependency

    registerFeature("guice") {
        usingSourceSet(sourceSets["main"])
    }

    registerFeature("junit") {
        usingSourceSet(sourceSets["main"])
    }

    registerFeature("yaml") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    api(projects.testngCoreApi)
    // Annotations have to be available on the compile classpath for the proper compilation
    api("com.google.code.findbugs:jsr305:_")
    api("com.beust:jcommander:_")

    "guiceApi"(platform("com.google.inject:guice-bom:_"))
    "guiceApi"("com.google.inject:guice")
    "junitImplementation"(projects.testngRunnerJunit4)
    "yamlImplementation"("org.yaml:snakeyaml:_")

    implementation(projects.testngCollections)
    implementation(projects.testngReflectionUtils)
    implementation(projects.testngRunnerApi)
    implementation("org.webjars:jquery:_")

    testImplementation(projects.testngAsserts)
    testImplementation("org.codehaus.groovy:groovy-all:_")
    testImplementation("org.spockframework:spock-core:_")
    testImplementation("org.apache-extras.beanshell:bsh:_")
    testImplementation("org.mockito:mockito-core:_")
    testImplementation("org.jboss.shrinkwrap:shrinkwrap-api:_")
    testImplementation("org.jboss.shrinkwrap:shrinkwrap-impl-base:_")
    testImplementation("org.xmlunit:xmlunit-assertj:_")
}

tasks.test {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    (testFramework.options as TestNGOptions).apply {
        suites("src/test/resources/testng.xml")
        listeners.add("org.testng.reporters.FailedInformationOnConsoleReporter")
        maxHeapSize = "1500m"
    }
}

//
// Releases:
// ./gradlew publish (to Sonatype, then go to https://oss.sonatype.org/index.html#stagingRepositories to publish)
//

publishing {
    repositories {
        maven {
            name = "sonatype"
            url = if (project.version.toString().contains("SNAPSHOT"))
                uri("https://oss.sonatype.org/content/repositories/snapshots/") else
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("sonatypeUser")?.toString() ?: System.getenv("SONATYPE_USER")
                password = project.findProperty("sonatypePassword")?.toString() ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}
