plugins {
    id("testng.java-library")
}

dependencies {
    api("org.apache.ant:ant:1.10.12")

    implementation(projects.testngCore)
    testImplementation(projects.testngAsserts)
    testImplementation("org.apache.ant:ant-testutil:1.10.12")
}
