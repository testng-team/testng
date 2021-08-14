plugins {
    id("testng.repositories")
    id("com.github.vlsi.stage-vote-release")
    id("idea")
}

val String.v: String get() = rootProject.extra["$this.version"] as String
val buildVersion = "testng".v + releaseParams.snapshotSuffix
version = buildVersion

println("Building testng $buildVersion")

/**
 * Release procedure:
 *   1. ./gradlew prepareVote -Prc=1 -Pgh    (builds artifacts, stages them to Central, closes staging repository)
 *   2. ./gradlew publishDist -Prc=1 -Pgh    (publishes staging repository and pushes release tag)
 *
 * See https://github.com/vlsi/vlsi-release-plugins#stage-vote-release-plugin
 * The following properties (e.g. $HOME/.gradle/gradle.properties) configure credentials.
 * The plugin would raise a warning if the property is not found.
 *   signing.gnupg.keyName=...
 *   signing.password=...
 *   signing.secretKeyRingFile=...
 *   ghNexusUsername=...
 *   ghNexusPassword=...
 *   ghGitSourceUsername=...
 *   ghGitSourcePassword=...
 *
 * You can use https://github.com/vlsi/asflike-release-environment as a playground to dry run releases.
 */

fun property(name: String) =
    providers.gradleProperty(name).forUseAtConfigurationTime()

releaseParams {
    tlp.set(property("github.repository"))
    organizationName.set(property("github.organization"))
    componentName.set(property("project.name"))
    prefixForProperties.set("gh")
    svnDistEnabled.set(false)
    sitePreviewEnabled.set(false)
    releaseTag.set(buildVersion) // or "testng-$buildVersion"
    nexus {
        packageGroup.set(property("nexus.profile"))
        mavenCentral()
    }
    voteText.set {
        """
        ${it.componentName} v${it.version}-rc${it.rc} is ready for preview.

        Git SHA: ${it.gitSha}
        Staging repository: ${it.nexusRepositoryUri}
        """.trimIndent()
    }
}

