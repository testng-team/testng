package buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.the

/**
 * Walks over the dependency tree and selects the first non-shaded dependencies.
 */
fun ResolvedComponentResult.filterFirstLayerDependenciesTo(
    set: MutableSet<ResolvedVariantResult>,
    isShaded: (ResolvedVariantResult) -> Boolean
): MutableSet<ResolvedVariantResult> {
    for (dependency in dependencies) {
        when (dependency) {
            is UnresolvedDependencyResult -> throw IllegalStateException(
                "Unresolved dependency $dependency: ${dependency.failure}",
                dependency.failure
            )
            is ResolvedDependencyResult -> {
                val resolvedVariant = dependency.resolvedVariant
                if (resolvedVariant in set) {
                    continue
                }
                if (isShaded(resolvedVariant)) {
                    dependency.selected.filterFirstLayerDependenciesTo(set, isShaded)
                    continue
                }
                // Ok, we detected the first non-shaded dependency, so we do not need to dig its dependencies
                set += resolvedVariant
            }
        }
    }
    return set
}

fun ResolvedComponentResult.filterFirstLayerDependencies(isShaded: (ResolvedVariantResult) -> Boolean) =
    filterFirstLayerDependenciesTo(mutableSetOf(), isShaded)

/**
 * Prepares a configuration that selects the first non-shaded dependencies:
 *  1. It resolves "AllDependencies" configuration
 *  2. Then the function walks over the dependency tree and finds the first non-shaded dependencies
 *  3. Then it converts the resolution results to dependency notation
 *  4. The collected dependencies are added to "FirstNonMergedDependencies" configuration.
 * Note: the trigger for all this computation is withDependencies, so the resolution performed only when it is required.
 */
fun Project.firstLayerDependencies(
    usage: String,
    conf: Configuration,
    vararg rest: Configuration,
): Configuration {
    val optionalFeatures = (the<JavaPluginExtension>() as ExtensionAware).the<OptionalFeaturesExtension>()
    val usageKind = usage.removePrefix("java-").capitalize()

    val allDependencies = configurations.create(conf.name + "${usageKind}AllDependencies") {
        description = "Resolves the list of all dependencies for $usage in ${conf.name}"
        isCanBeConsumed = false
        isCanBeResolved = true
        isVisible = false
        extendsFrom(conf)
        extendsFrom(*rest)
        javaLibrary(objects, usage)
    }

    return configurations.create(conf.name + "${usageKind}FirstNonMergedDependencies") {
        description = "Resolves the list of external dependencies for $usage in ${conf.name}"
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
        isVisible = false
        javaLibrary(objects, usage)
        withDependencies {
            // Clear any user-added-by-mistake dependencies
            clear()
            addAll(
                allDependencies.incoming.resolutionResult.root
                    .filterFirstLayerDependencies(optionalFeatures.shadedDependenciesFilter.get())
                    .map { project.dependencies.reconstruct(it) }
            )
        }
    }
}
