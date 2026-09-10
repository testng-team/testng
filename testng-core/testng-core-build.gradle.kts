import buildlogic.VerifyTestExecution
import buildlogic.registerOptionalFeatureVariants

plugins {
    id("testng.java-library")
    id("testng.kotlin-library")
    groovy
    id("testng.sonarqube")
    id("testng.test-execution")
}

// Optional features: the transitive dependency is exposed only to consumers that ask for the
// matching capability.
registerOptionalFeatureVariants("guice", buildParameters.targetJavaVersion, tasks.jar)

tasks.withType<GroovyCompile>().configureEach {
    // Groovy does not support targeting Java release yet
    // See https://issues.apache.org/jira/browse/GROOVY-11105
    sourceCompatibility = buildParameters.targetJavaVersion.toString()
    targetCompatibility = buildParameters.targetJavaVersion.toString()
}


dependencies {
    api(projects.testngCoreApi)

    "guiceApi"(platform("com.google.inject:guice-bom:6.0.0"))
    "guiceApi"("com.google.inject:guice")

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
    // Classes under org.testng.**.samples are TestNG input, not tests: a driver builds a suite
    // around them and asserts on the result. Several are meant to fail or to be skipped, so
    // running them directly reports failures that mean nothing. The exclude is inert while the
    // suite XML below decides what runs, and is what makes GitHub issue #3446 step 5 -- dropping
    // that XML for classpath discovery -- a one-line change rather than a fresh investigation.
    exclude("org/testng/**/samples/**")
    (testFramework.options as TestNGOptions).apply {
        suites("src/test/resources/testng.xml")
        maxHeapSize = "1500m"
    }
}

// A test that forks a child JVM with a heap of its own cannot live in testng.xml: that suite is
// handed to every Gradle fork, so the class would run once per fork and start as many children at
// once. This task runs the forking suite on its own, one fork at a time.
val memoryTest by
    tasks.registering(Test::class) {
        description = "Runs the tests that fork a child JVM, one at a time."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        maxParallelForks = 1
        useTestNG {
            suites("src/test/resources/testng-memory.xml")
            maxHeapSize = "1500m"
        }
    }

// The project-wide verifyTestExecution is wired to tasks.test: it reads testng.xml and
// build/test-results/test, so it cannot see a suite that names neither. Without this second one a
// class that fell out of testng-memory.xml would leave the build green and silent -- which is the
// failure that check exists to catch, arriving by a different route. It shares
// execution-known-silent.txt because the memory suite has no silent classes and one list is one
// place to look; an entry there would have to name a class of this suite to affect it.
val verifyMemoryTestExecution by
    tasks.registering(VerifyTestExecution::class) {
        description = "Verifies the memory suite ran every class it names, and did not change"
        group = LifecycleBasePlugin.VERIFICATION_GROUP

        dependsOn(memoryTest)
        suite.set(layout.projectDirectory.file("src/test/resources/testng-memory.xml"))
        inventory.set(layout.projectDirectory.file("execution-inventory-memory.txt"))
        knownSilent.set(layout.projectDirectory.file("execution-known-silent.txt"))
        results.set(layout.buildDirectory.dir("test-results/memoryTest"))
        update.set(providers.gradleProperty("updateExecutionInventory").map { true }.orElse(false))
    }

tasks.check {
    dependsOn(memoryTest, verifyMemoryTestExecution)
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
