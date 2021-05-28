plugins {
    id("org.sonarqube")
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io/")
        property("sonar.organization", "testng-team")
        property("sonar.github.repository", "cbeust/testng")
        property("sonar.github.login", "testng-bot")
    }
}
