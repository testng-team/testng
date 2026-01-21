import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.signing
import org.gradle.kotlin.dsl.the

plugins {
    signing
    id("build-logic.build-params")
}

plugins.withId("signing") {
    configure<SigningExtension> {
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project

        // Support both project properties and environment variables for CI/CD
        val pgpKey = signingKey ?: System.getenv("SIGNING_PGP_PRIVATE_KEY")
        val pgpPassphrase = signingPassword ?: System.getenv("SIGNING_PGP_PASSPHRASE")

        if (pgpKey != null && pgpPassphrase != null) {
            useInMemoryPgpKeys(signingKeyId, pgpKey, pgpPassphrase)
        }

        // Only sign if signing is enabled (controlled by build parameter)
        val signingEnabled = providers.gradleProperty("signing.pgp.enabled").orElse("AUTO")
        val isRelease = providers.gradleProperty("release").map { it.toBoolean() }.orElse(false)
        isRequired = signingEnabled.get() == "AUTO" && isRelease.get()
    }
}

// Sign all publications when maven-publish plugin is applied
plugins.withId("maven-publish") {
    configure<SigningExtension> {
        afterEvaluate {
            sign(the<PublishingExtension>().publications)
        }
    }
}
