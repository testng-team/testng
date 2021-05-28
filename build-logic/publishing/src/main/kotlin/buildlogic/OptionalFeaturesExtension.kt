package buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
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
            it.owner.let { id -> id is ProjectComponentIdentifier && id.build.isCurrentBuild }
        }
    }

    fun create(name: String, builder: OptionalFeatureBuilder.() -> Unit) {
        project.the<JavaPluginExtension>().registerFeature(name) {
            val sourceSets: SourceSetContainer by project
            usingSourceSet(sourceSets["main"])
        }

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

        // By default Gradle adds the jar as artifact, however, we won't need it
        // We'll put merged jar later as a main artifact
        project.configurations {
            get("${name}ApiElements").apply {
                // This effectively adds "optional" pom dependencies for scope=compile
                extendsFrom(project.firstLayerDependencies(Usage.JAVA_API, declaredApi))
                artifacts.clear()
                // The feature do not provide their own classes or resources
                // All the feature resources would come from the dependencies
                outgoing.variants.removeIf { it.name == "classes" || it.name == "resources" }
            }
            get("${name}RuntimeElements").apply {
                // This effectively adds "optional" pom dependencies for scope=runtime
                extendsFrom(project.firstLayerDependencies(Usage.JAVA_RUNTIME, declaredRuntime))
                artifacts.clear()
                outgoing.variants.removeIf { it.name == "classes" || it.name == "resources" }
            }
        }
    }
}
