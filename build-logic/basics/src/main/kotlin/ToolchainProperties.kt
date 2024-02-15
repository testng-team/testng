import buildparameters.BuildParametersExtension
import org.gradle.api.JavaVersion

class ToolchainProperties(
    val version: Int,
    val vendor: String?,
    val implementation: String?,
)

val BuildParametersExtension.buildJdk: ToolchainProperties?
    get() = jdkBuildVersion.takeIf { it != 0 }
        ?.let { ToolchainProperties(it, jdkBuildVendor.orNull, jdkBuildImplementation.orNull) }

val BuildParametersExtension.buildJdkVersion: Int
    get() = buildJdk?.version ?: JavaVersion.current().majorVersion.toInt()

val BuildParametersExtension.testJdk: ToolchainProperties?
    get() = jdkTestVersion.orNull?.takeIf { it != 0 }
        ?.let { ToolchainProperties(it, jdkTestVendor.orNull, jdkTestImplementation.orNull) }
        ?: buildJdk
