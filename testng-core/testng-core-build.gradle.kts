import buildlogic.registerOptionalFeatureVariants

plugins {
    id("testng.java-library")
    id("testng.kotlin-library")
    groovy
    id("testng.sonarqube")
}

// Optional features: the transitive dependency is exposed only to consumers that ask for the
// matching capability.
registerOptionalFeatureVariants("guice", buildParameters.targetJavaVersion, tasks.jar)
registerOptionalFeatureVariants("yaml", buildParameters.targetJavaVersion, tasks.jar)

tasks.withType<GroovyCompile>().configureEach {
    // Groovy does not support targeting Java release yet
    // See https://issues.apache.org/jira/browse/GROOVY-11105
    sourceCompatibility = buildParameters.targetJavaVersion.toString()
    targetCompatibility = buildParameters.targetJavaVersion.toString()
}


dependencies {
    api(projects.testngCoreApi)
    // Annotations have to be available on the compile classpath for the proper compilation
    compileOnly("com.github.spotbugs:spotbugs:4.10.3")

    "guiceApi"(platform("com.google.inject:guice-bom:6.0.0"))
    "guiceApi"("com.google.inject:guice")
    "yamlImplementation"("org.yaml:snakeyaml:2.6")

    implementation(projects.testngCollections)
    implementation(projects.testngReflectionUtils)
    implementation(projects.testngRunnerApi)
    testImplementation("org.testng:testng-asserts:1.0.0")
    testImplementation(projects.testngTestKit)
    testImplementation("org.apache.groovy:groovy-all:5.0.7") {
        exclude("org.testng", "testng")
    }
    testImplementation("org.apache-extras.beanshell:bsh:2.0b6")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.xmlunit:xmlunit-assertj:2.13.0")
    testImplementation("in.jlibs:jlibs-core:3.0.1")
    testImplementation("org.gridkit.jvmtool:heaplib:0.2")
    testImplementation("org.gridkit.lab:jvm-attach-api:1.5")
    testImplementation("commons-io:commons-io:2.22.0")
}

tasks.compileTestGroovy {
    dependsOn(tasks.compileTestKotlin)
    classpath += files(tasks.compileTestKotlin)
}

tasks.test {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    (testFramework.options as TestNGOptions).apply {
        suites("src/test/resources/testng.xml")
        maxHeapSize = "1500m"
    }
}

// <editor-fold defaultstate="collapsed" desc="Bundle jQuery from the webjar">
// The HTML reporter serves jQuery from its own resources so reports work offline. Extract it from
// the webjar at build time rather than checking the minified file in: the version then lives in a
// single place, and the file cannot drift from the declared dependency.
// The configuration is resolvable only, so jQuery stays out of the published pom -- TestNG has no
// runtime dependency on it.
val jquery = configurations.dependencyScope("jquery") {
    description = "The jQuery webjar the HTML reporter bundles"
}
val jqueryClasspath = configurations.resolvable("jqueryClasspath") {
    extendsFrom(jquery.get())
}

dependencies {
    add(jquery.name, "org.webjars:jquery:4.0.0")
}

val extractJquery = tasks.register<Sync>("extractJquery") {
    description = "Extracts jquery.min.js from the webjar into the reporter's resources"
    from(jqueryClasspath.map { zipTree(it.singleFile) }) {
        include("META-INF/resources/webjars/jquery/*/jquery.min.js")
        eachFile { path = "org/testng/jquery.min.js" }
        includeEmptyDirs = false
    }
    into(layout.buildDirectory.dir("generated/jquery"))
}

sourceSets.main {
    output.dir(mapOf("builtBy" to extractJquery), layout.buildDirectory.dir("generated/jquery"))
}
// </editor-fold>
