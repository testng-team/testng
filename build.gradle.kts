plugins {
    id("testng.repositories")
    id("idea")
    id("com.gradleup.nmcp.aggregation") version "1.0.2"
}

val String.v: String get() = rootProject.extra["$this.version"] as String
val buildVersion = "testng".v
version = buildVersion

println("Building testng $buildVersion")

tasks.register("parameters") {
    group = HelpTasksPlugin.HELP_GROUP
    description = "Displays the supported build parameters."
    dependsOn(gradle.includedBuild("build-logic").task(":build-parameters:parameters"))
}

// Configure Maven Central Portal publishing
nmcpAggregation {
    centralPortal {
        username.set(providers.environmentVariable("CENTRAL_PORTAL_USERNAME"))
        password.set(providers.environmentVariable("CENTRAL_PORTAL_PASSWORD"))
        publishingType.set(providers.gradleProperty("centralPortal.publishingType").orElse("AUTOMATIC"))
    }
    // Publish all projects that apply the 'maven-publish' plugin
    publishAllProjectsProbablyBreakingProjectIsolation()
}

/**
 * Release procedure:
 *   Publishing to Maven Central is now done via the Central Portal API using the com.gradleup.nmcp plugin.
 *
 *   For releases:
 *     Use the GitHub Actions workflow: .github/workflows/publish-maven-central.yml
 *     This will build, sign, and publish artifacts to Maven Central Portal.
 *
 *   For snapshots:
 *     Snapshots are automatically published on push to master via .github/workflows/publish-snapshot.yml
 *
 *   Manual publishing (if needed):
 *     ./gradlew publishAggregationToCentralPortal -Prelease=true
 *
 *   Required environment variables for publishing:
 *     CENTRAL_PORTAL_USERNAME - Your Sonatype account username
 *     CENTRAL_PORTAL_PASSWORD - Your Sonatype account password
 *     SIGNING_PGP_PRIVATE_KEY - PGP private key for signing artifacts
 *     SIGNING_PGP_PASSPHRASE  - Passphrase for the PGP private key
 */

