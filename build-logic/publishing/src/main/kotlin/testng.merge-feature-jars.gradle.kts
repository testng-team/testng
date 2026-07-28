import buildlogic.OptionalFeaturesExtension
import buildlogic.firstLayerDependencies
import buildlogic.javaLibrary
import buildlogic.reconstruct
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-base`
    `reporting-base`
    id("testng.published-java-library")
}

val optionalFeatures = (the<JavaPluginExtension>() as ExtensionAware).extensions
    .create<OptionalFeaturesExtension>("optionalFeatures", project)

inline fun <reified T : Named> AttributeContainer.attribute(attr: Attribute<T>, value: String) =
    attribute(attr, objects.named(value))

val shadedDependencyElements = configurations.create("shadedDependencyElements") {
    description = "Declares which modules to aggregate into ...-all.jar"
    isCanBeConsumed = false
    isCanBeResolved = false
}

fun Configuration.javaLibraryRuntime() = javaLibrary(objects, Usage.JAVA_RUNTIME)

configurations["api"].extendsFrom(
    firstLayerDependencies(
        Usage.JAVA_API,
        shadedDependencyElements
    )
)

configurations["implementation"].extendsFrom(
    firstLayerDependencies(
        Usage.JAVA_RUNTIME,
        shadedDependencyElements
    )
)

val shadedDependencyFullRuntimeClasspath = configurations.create("shadedDependencyFullRuntimeClasspath") {
    description = "Resolves the list of shadedDependencyElements to testng and external dependencies"
    isCanBeConsumed = false
    isCanBeResolved = true
    isVisible = false
    extendsFrom(shadedDependencyElements)
    javaLibraryRuntime()
}

val mergedJars = configurations.create("mergedJars") {
    description = "Resolves the list of testng modules to include into -all jar"
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
    javaLibraryRuntime()
    withDependencies {
        // Clear any user-added-by-mistake dependencies
        clear()
        // Identifies TestNG projects in shadedDependencyFullRuntimeClasspath dependency tree
        addAll(
            shadedDependencyFullRuntimeClasspath.incoming.resolutionResult.allDependencies
                .asSequence()
                .filter { !it.isConstraint }
                .filterIsInstance<ResolvedDependencyResult>()
                .mapNotNull { resolved ->
                    resolved.resolvedVariant
                        .takeIf { optionalFeatures.shadedDependenciesFilter.get()(it) }
                        ?.let { project.dependencies.reconstruct(it) }
                }
        )
    }
}

val shadedDependencyJavadocClasspath = configurations.create("shadedDependencyJavadocClasspath") {
    description = "Resolves a runtime classpath of the aggregated -all dependenices"
    isCanBeConsumed = false
    isCanBeResolved = true
    isVisible = false
    extendsFrom(mergedJars)
    extendsFrom(configurations["compileClasspath"])
    extendsFrom(shadedDependencyFullRuntimeClasspath)
    javaLibraryRuntime()
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, LibraryElements.JAR)
        attribute(Bundling.BUNDLING_ATTRIBUTE, Bundling.EXTERNAL)
    }
}

val mergedJar = tasks.register<ShadowJar>("mergedJar") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Builds all-project jar (third-party dependencies are left as is)"
    configurations = listOf(mergedJars)
    // Individual jars have their own license, and ShadowJar overrides "duplicateStrategy" option
    // So we exclude LICENSE.txt from the merged jars, and let testng.java.gradle.kts to add the default one
    exclude("META-INF/LICENSE.txt")
    archiveClassifier.set("all")
}

dependencies {
    "implementation"(files(mergedJar))
}

val sourcesToMerge = configurations.create("sourcesToMerge") {
    description = "Resolves the list of source directories to include into sources-all jar"
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false // jarsToMerge is a full set of modules, so no need to have transitivity here
    extendsFrom(mergedJars)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, Usage.JAVA_RUNTIME)
        attribute(Category.CATEGORY_ATTRIBUTE, Category.DOCUMENTATION)
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, "source-folders")
    }
}

val mergedSourcesJar = tasks.register<Jar>("mergedSourcesJar") {
    from(sourcesToMerge.incoming.artifactView { lenient(true) }.files)
    archiveClassifier.set("sources-all")
}

val mergedJavadoc = tasks.register<Javadoc>("mergedJavadoc") {
    description = "Generates an aggregate javadoc"
    group = LifecycleBasePlugin.BUILD_GROUP
    setSource(sourcesToMerge.incoming.artifactView { lenient(true) }.files)
    include("**/*.java")
    setDestinationDir(reporting.baseDirectory.dir("mergedJavadoc").get().asFile)
    classpath = shadedDependencyJavadocClasspath
}

val mergedJavadocJar = tasks.register<Jar>("mergedJavadocJar") {
    description = "Generates an aggregate javadoc jar"
    group = LifecycleBasePlugin.BUILD_GROUP
    from(mergedJavadoc)
    archiveClassifier.set("javadoc-all")
}

// Configure merged artifacts for publication
configurations.named("sourcesElements") {
    artifacts.clear()
    outgoing.artifact(mergedSourcesJar) {
        classifier = "sources"
    }
}

configurations.named("javadocElements") {
    artifacts.clear()
    outgoing.artifact(mergedJavadocJar) {
        classifier = "javadoc"
    }
}

for (name in listOf("apiElements", "runtimeElements")) {
    configurations.named(name) {
        artifacts.clear()
        outgoing.artifact(mergedJar) {
            classifier = null
        }
        outgoing.variants.removeIf { it.name == "classes" || it.name == "resources" }
    }
}
