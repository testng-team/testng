plugins {
    id("testng.java-library")
    id("testng.kotlin-library")
    groovy
    id("testng.sonarqube")
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
    compileOnly("com.github.spotbugs:spotbugs:_")
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
    testImplementation("in.jlibs:jlibs-core:_")
    testImplementation("org.gridkit.jvmtool:heaplib:_")
    testImplementation("org.gridkit.lab:jvm-attach-api:_")
    testImplementation("commons-io:commons-io:_")
}

tasks.compileTestGroovy {
    dependsOn(tasks.compileTestKotlin)
    classpath += files(tasks.compileTestKotlin)
}
tasks.compileTestKotlin {
    classpath = sourceSets.test.get().compileClasspath
}

tasks.test {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    (testFramework.options as TestNGOptions).apply {
        suites("src/test/resources/testng.xml")
        listeners.add("org.testng.reporters.FailedInformationOnConsoleReporter")
        maxHeapSize = "1500m"
    }
}
