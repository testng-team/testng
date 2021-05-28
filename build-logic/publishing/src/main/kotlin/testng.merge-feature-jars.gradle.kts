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

val shadedDependencyElements by configurations.creating {
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

val shadedDependencyFullRuntimeClasspath by configurations.creating {
    description = "Resolves the list of shadedDependencyElements to testng and external dependencies"
    isCanBeConsumed = false
    isCanBeResolved = true
    isVisible = false
    extendsFrom(shadedDependencyElements)
    javaLibraryRuntime()
}

val mergedJars by configurations.creating {
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

val shadedDependencyJavadocClasspath by configurations.creating {
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

val mergedJar by tasks.registering(ShadowJar::class) {
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

val sourcesToMerge by configurations.creating {
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

val mergedSourcesJar by tasks.registering(Jar::class) {
    from(sourcesToMerge.incoming.artifactView { lenient(true) }.files)
    archiveClassifier.set("sources-all")
}

val mergedJavadoc by tasks.registering(Javadoc::class) {
    description = "Generates an aggregate javadoc"
    group = LifecycleBasePlugin.BUILD_GROUP
    setSource(sourcesToMerge.incoming.artifactView { lenient(true) }.files)
    include("**/*.java")
    setDestinationDir(reporting.file("mergedJavadoc"))
    classpath = shadedDependencyJavadocClasspath
}

val mergedJavadocJar by tasks.registering(Jar::class) {
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
