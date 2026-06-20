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

    registerFeature("yaml") {
        usingSourceSet(sourceSets["main"])
    }
}

tasks.withType<GroovyCompile>().configureEach {
    // Groovy does not support targeting Java release yet
    // See https://issues.apache.org/jira/browse/GROOVY-11105
    sourceCompatibility = buildParameters.targetJavaVersion.toString()
    targetCompatibility = buildParameters.targetJavaVersion.toString()
}


dependencies {
    api(projects.testngCoreApi)
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    // Annotations have to be available on the compile classpath for the proper compilation
    compileOnly("com.github.spotbugs:spotbugs:4.8.1")
    api("org.jcommander:jcommander:1.83")

    "guiceApi"(platform("com.google.inject:guice-bom:5.1.0"))
    "guiceApi"("com.google.inject:guice")
    "yamlImplementation"("org.yaml:snakeyaml:2.2")

    implementation(projects.testngCollections)
    implementation(projects.testngReflectionUtils)
    implementation(projects.testngRunnerApi)
    implementation("org.webjars:jquery:3.7.1")
    testImplementation(projects.testngAsserts)
    // Groovy 4.x is required to support Java 21 bytecode (class file major version 65)
    // Groovy 3.x doesn't support reading Java 21 bytecode
    testImplementation("org.apache.groovy:groovy-all:4.0.29") {
        exclude("org.testng", "testng")
    }
    testImplementation("org.apache-extras.beanshell:bsh:2.0b6")
    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("org.jboss.shrinkwrap:shrinkwrap-api:1.2.6")
    testImplementation("org.jboss.shrinkwrap:shrinkwrap-impl-base:1.2.6")
    testImplementation("org.xmlunit:xmlunit-assertj:2.9.1")
    testImplementation("in.jlibs:jlibs-core:3.0.1")
    testImplementation("org.gridkit.jvmtool:heaplib:0.2")
    testImplementation("org.gridkit.lab:jvm-attach-api:1.5")
    testImplementation("commons-io:commons-io:2.15.0")
}

tasks.compileTestGroovy {
    dependsOn(tasks.compileTestKotlin)
    classpath += files(tasks.compileTestKotlin)
}
// Note: In Kotlin 2.3.0+, the classpath property on KotlinCompile task has been removed.
// The classpath is now automatically managed through source sets, so no manual configuration is needed.

tasks.test {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    (testFramework.options as TestNGOptions).apply {
        suites("src/test/resources/testng.xml")
        maxHeapSize = "1500m"
    }
}
