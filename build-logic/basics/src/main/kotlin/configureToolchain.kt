import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec

fun JavaToolchainService.launcherFor(jdk: ToolchainProperties): Provider<JavaLauncher> = launcherFor {
    configureToolchain(jdk)
}

fun JavaToolchainSpec.configureToolchain(jdk: ToolchainProperties?) {
    if (jdk == null) {
        return
    }
    languageVersion.set(JavaLanguageVersion.of(jdk.version))
    jdk.vendor?.let {
        vendor.set(JvmVendorSpec.matching(it))
    }
    if (jdk.implementation.equals("J9", ignoreCase = true)) {
        implementation.set(JvmImplementation.J9)
    }
}
