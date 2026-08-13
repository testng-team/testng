plugins {
    id("testng.repositories")
    id("build-logic.build-params")
    id("idea")
    id("com.gradleup.nmcp.aggregation") version "1.6.1"
    id("org.openrewrite.rewrite") version "7.38.0"
}

dependencies {
    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.36.0"))
    rewrite("org.openrewrite.recipe:rewrite-static-analysis")
    rewrite("org.openrewrite.recipe:rewrite-migrate-java")
}

// One-shot profile for the org.testng.collections migration, run by hand with
// ./gradlew rewriteRun -PmigrateCollections. It is deleted, together with the recipe it activates,
// once the migration has landed; CI never sees it.
val migratingCollections = providers.gradleProperty("migrateCollections").isPresent

rewrite {
    // The recipe list lives in rewrite.yml, which documents why the upstream composites
    // (CommonStaticAnalysis, CodeCleanup, Java8toJava11) are not used directly.
    // The migration profile swaps the recipe rather than only widening the exclusions below:
    // ModernizeMainSources must not be let loose on the test fixtures.
    activeRecipe(
        if (migratingCollections) "org.testng.build.MigrateOffTestngCollections"
        else "org.testng.build.ModernizeMainSources"
    )

    // Makes OpenRewrite lay imports out the way google-java-format does, so its output does not
    // have to be corrected by autostyleApply. rewrite.yml explains the measurement behind this.
    activeStyle("org.testng.build.ImportLayout")

    // exclusion() matches file paths, not recipe names.
    exclusion(
        *buildList {
            // Test sources are out of scope: test/** mixes real tests with fixture classes whose
            // method names, declaration order and finalize() presence are what the surrounding
            // tests assert on, and testng.xml references test classes by FQN.
            // The collections migration is the exception: 157 of its call sites are in
            // testng-core/src/test, and rewriting a Lists.newArrayList() there is not the kind of
            // change those fixtures are sensitive to.
            // Braces are not optional here: "**/*.gradle.kts" below does not match this file, the
            // root build script, so NeedBraces would rewrite a brace-less `if` and the CI dry-run
            // gate would fail on it.
            if (!migratingCollections) {
                add("**/src/test/**")
            }
            // Conversely, the migration must not rewrite the factories' own bodies -- they call
            // each other, and the whole point is to leave them intact for third parties.
            if (migratingCollections) {
                add("**/testng-collections/**")
            }
            // Nothing here rewrites build scripts, and .gradle.kts would be routed to the
            // experimental Kotlin parser for no benefit. This also covers build-logic/ and
            // build-logic-commons/, whose tracked files are all .kt or .gradle.kts.
            addAll(listOf("**/*.gradle.kts", "**/*.kt", "**/*.groovy"))
        }.toTypedArray()
    )

    // rewriteDryRun is a manual maintenance task by default. CI turns this on so the gate is
    // the Gradle task itself, which means ./gradlew rewriteDryRun -PfailOnRewriteDryRun=true
    // reproduces the CI check exactly.
    failOnDryRunResults = buildParameters.failOnRewriteDryRun
}

val String.v: String get() = rootProject.extra["$this.version"] as String
val baseVersion = "testng".v
val isRelease = providers.gradleProperty("release").map { it.toBoolean() }.orElse(false).get()
val buildVersion = if (isRelease) baseVersion else "$baseVersion-SNAPSHOT"
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
}

// Projects to publish, listed explicitly. nmcp's
// publishAllProjectsProbablyBreakingProjectIsolation() would discover them, but it feeds Project
// objects to the dependency handler, and that notation is removed in Gradle 10.
dependencies {
    nmcpAggregation(project(":testng"))
}

// Guard against the list above drifting: a project that starts publishing without being aggregated
// would silently be missing from the Central Portal deployment.
gradle.projectsEvaluated {
    val aggregated = configurations.getByName("nmcpAggregation")
        .dependencies
        .filterIsInstance<ProjectDependency>()
        .map { it.path }
        .toSet()
    val missing = subprojects
        .filter { it.plugins.hasPlugin("maven-publish") }
        .map { it.path }
        .filterNot { it in aggregated }
    require(missing.isEmpty()) {
        "These projects apply 'maven-publish' but are not aggregated for Maven Central: $missing. " +
            "Add nmcpAggregation(project(\"<path>\")) to the dependencies block in build.gradle.kts."
    }
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

