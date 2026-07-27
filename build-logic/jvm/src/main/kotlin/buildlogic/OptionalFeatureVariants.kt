package buildlogic

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

/**
 * Declares one consumable variant of an optional feature, replacing what
 * `registerFeature(name) { usingSourceSet(sourceSets["main"]) }` used to emit.
 *
 * Gradle 10 removes that form, and the prescribed replacement -- a source set of its own -- does
 * not fit what these features are for: they carry no code, only extra dependencies attached to the
 * *main* artifact. A dedicated source set would publish an empty jar under the feature capability
 * and take the dependencies off main's compile classpath.
 *
 * @param usage `"Api"` or `"Runtime"`; also drives the configuration name and the usage attribute.
 * @param artifact the jar to attach, or null to expose dependencies only.
 */
fun Project.optionalFeatureElements(
    featureName: String,
    usage: String,
    targetJvmVersion: Int,
    artifact: TaskProvider<out Jar>? = null,
    extendsFrom: List<Configuration> = emptyList(),
): NamedDomainObjectProvider<out Configuration> {
    // Captured here on purpose: inside the configuration block, 'name' is the configuration's.
    val featureCapability = "$group:$name-$featureName:$version"
    val usageAttribute = if (usage == "Api") Usage.JAVA_API else Usage.JAVA_RUNTIME
    val displayName = if (usage == "Api") "API" else usage

    return configurations.consumable("$featureName${usage}Elements") {
        description = "$displayName elements for the '$featureName' feature."
        extendsFrom(*extendsFrom.toTypedArray())
        attributes {
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, targetJvmVersion)
            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(usageAttribute))
        }
        outgoing {
            artifact?.let { artifact(it) }
            capability(featureCapability)
        }
    }
}

/**
 * Publishes [apiElements] and [runtimeElements] as optional variants of the `java` component, so
 * the feature's dependencies land in the pom marked `<optional>true</optional>`.
 */
fun Project.publishOptionalFeature(
    apiElements: NamedDomainObjectProvider<out Configuration>,
    runtimeElements: NamedDomainObjectProvider<out Configuration>,
) {
    plugins.withId("maven-publish") {
        components.named<AdhocComponentWithVariants>("java") {
            addVariantsFromConfiguration(apiElements.get()) {
                mapToMavenScope("compile")
                mapToOptional()
            }
            addVariantsFromConfiguration(runtimeElements.get()) {
                mapToMavenScope("runtime")
                mapToOptional()
            }
        }
    }
}

/**
 * Declares an optional feature whose dependencies are attached to the main artifact: creates the
 * `<feature>Api` / `<feature>Implementation` dependency scopes, the two consumable variants, and
 * keeps the dependencies on main's compile classpath.
 */
fun Project.registerOptionalFeatureVariants(
    featureName: String,
    targetJvmVersion: Int,
    artifact: TaskProvider<out Jar>,
) {
    val declaredApi = configurations.dependencyScope("${featureName}Api") {
        description = "API dependencies for the '$featureName' feature."
    }.get()
    val declaredImplementation = configurations.dependencyScope("${featureName}Implementation") {
        description = "Implementation dependencies for the '$featureName' feature."
    }.get()

    val apiElements =
        optionalFeatureElements(featureName, "Api", targetJvmVersion, artifact, listOf(declaredApi))
    val runtimeElements = optionalFeatureElements(
        featureName, "Runtime", targetJvmVersion, artifact,
        listOf(declaredApi, declaredImplementation),
    )

    // The classes that use the feature live in the main source set, so its dependencies have to
    // stay on main's compile classpath -- compileOnly, since consumers opt in through the
    // capability. Tests exercise those classes, so they need them at runtime too.
    configurations.named("compileOnly") {
        extendsFrom(declaredApi, declaredImplementation)
    }
    configurations.named("testImplementation") {
        extendsFrom(declaredApi, declaredImplementation)
    }

    publishOptionalFeature(apiElements, runtimeElements)
}
