plugins {
    id("build-logic.kotlin-dsl-gradle-plugin")
}

dependencies {
    api(projects.buildParameters)
    api(projects.basics)
    api("org.sonarqube:org.sonarqube.gradle.plugin:7.3.1.8318")
    // Pinned: 4.0.1 fails on JDK 25. See the ignore entry in .github/dependabot.yml and
    // https://github.com/testng-team/testng/issues/3292
    api("com.github.autostyle:autostyle-plugin-gradle:4.0")
    api("net.ltgt.gradle:gradle-errorprone-plugin:5.1.0")
}
