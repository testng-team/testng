import org.gradle.kotlin.dsl.signing

plugins {
    signing
}

plugins.withId("signing") {
    configure<SigningExtension> {
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }
}
