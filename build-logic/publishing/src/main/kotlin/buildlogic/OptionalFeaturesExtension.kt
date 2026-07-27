package buildlogic

import buildparameters.BuildParametersExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the

/**
 * DSL for declaring optional features. The dependencies are stored to plugin-local "declared-*" configurations
 */
class OptionalFeatureBuilder(
    private val dependencyHandler: DependencyHandler,
    private val declaredApi: Configuration,
    private val declaredImplementation: Configuration
) {
    fun platform(dependencyNotation: Any) =
        dependencyHandler.platform(dependencyNotation)

    fun api(dependencyNotation: Any) {
        dependencyHandler.add(declaredApi.name, dependencyNotation)
    }

    fun implementation(dependencyNotation: Any) {
        dependencyHandler.add(declaredImplementation.name, dependencyNotation)
    }
}

abstract class OptionalFeaturesExtension(private val project: Project) {
    // It allows to explicitly list which modules should be merged and which will be left alone as dependencies
    abstract val shadedDependenciesFilter: Property<(ResolvedVariantResult) -> Boolean>

    init {
        // By default, shade all modules from the current build
        shadedDependenciesFilter.convention {
            // BuildIdentifier.isCurrentBuild() was removed in Gradle 9; the root build's path is ":".
            it.owner.let { id -> id is ProjectComponentIdentifier && id.build.buildPath == ":" }
        }
    }

    fun create(name: String, builder: OptionalFeatureBuilder.() -> Unit) {
        val declaredApi = project.configurations.create("${name}DeclaredApi") {
            description = "Api dependencies for feature $name"
            isCanBeResolved = false
            isCanBeConsumed = false
        }
        val declaredImplementation = project.configurations.create("${name}DeclaredImplementation") {
            description = "Implementation dependencies for feature $name"
            isCanBeResolved = false
            isCanBeConsumed = false
        }
        val declaredRuntime = project.configurations.create("${name}DeclaredRuntime") {
            description = "Runtime dependencies for feature $name"
            isCanBeResolved = false
            isCanBeConsumed = false
            extendsFrom(declaredApi, declaredImplementation)
        }

        OptionalFeatureBuilder(
            project.dependencies,
            declaredApi,
            declaredImplementation
        ).builder()

        // This is to include all testng modules (even optional) to -all.jar
        project.configurations["shadedDependencyFullRuntimeClasspath"]
            .extendsFrom(declaredRuntime)

        // The variants carry no file of their own: the feature contributes dependencies only, and
        // the merged jar is attached later as the main artifact. They are declared by hand because
        // Gradle 10 removes registerFeature(name) { usingSourceSet(main) }.
        // Not JavaPluginExtension.targetCompatibility: the build sets --release instead, so that
        // reports the toolchain version (25) rather than the bytecode target.
        val targetJvmVersion = project.the<BuildParametersExtension>().targetJavaVersion
        val apiElements = project.optionalFeatureElements(
            name, "Api", targetJvmVersion,
            // This effectively adds "optional" pom dependencies for scope=compile
            extendsFrom = listOf(project.firstLayerDependencies(Usage.JAVA_API, declaredApi)),
        )
        val runtimeElements = project.optionalFeatureElements(
            name, "Runtime", targetJvmVersion,
            // This effectively adds "optional" pom dependencies for scope=runtime
            extendsFrom = listOf(project.firstLayerDependencies(Usage.JAVA_RUNTIME, declaredRuntime)),
        )
        project.publishOptionalFeature(apiElements, runtimeElements)
    }
}
