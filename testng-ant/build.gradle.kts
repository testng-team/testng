plugins {
    id("testng.published-java-library")
}

dependencies {
    api("org.apache.ant:ant:_")

    implementation(projects.testngCore)
    testImplementation(projects.testngAsserts)
    testImplementation("org.apache.ant:ant-testutil:_")
}
