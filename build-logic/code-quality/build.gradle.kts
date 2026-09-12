plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.buildParameters)
    api(projects.basics)
    api("org.sonarqube:org.sonarqube.gradle.plugin:7.4.0.8496")
    // Pinned: 4.0.1 fails on JDK 25. See the ignore entry in .github/dependabot.yml and
    // https://github.com/testng-team/testng/issues/3292
    api("com.github.autostyle:autostyle-plugin-gradle:4.0")
    api("net.ltgt.gradle:gradle-errorprone-plugin:5.1.0")
}

dependencies {
    // The repository's own test framework, so the build's checks are written the same way as the
    // code they guard. VerifyTestExecutionRulesTest needs nothing else: problemsIn() is a pure
    // function over sets and maps.
    //
    // A released version, pinned. build-logic builds TestNG, so it cannot depend on the TestNG it
    // is building. Raise this by hand when there is a reason to; nothing here tracks the project
    // version, and nothing should.
    testImplementation("org.testng:testng:7.11.0")
}

tasks.withType<Test>().configureEach {
    useTestNG()
}
