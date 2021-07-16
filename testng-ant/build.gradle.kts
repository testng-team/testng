plugins {
    id("testng.java-library")
}

dependencies {
    api("org.apache.ant:ant:_")

    implementation(projects.testngCore)
    implementation(projects.testngJcommander)
    testImplementation(projects.testngAsserts)
    testImplementation("org.apache.ant:ant-testutil:_")
}
