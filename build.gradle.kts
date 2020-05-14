allprojects {
    tasks.withType<Javadoc> {
        excludes.add("org/testng/internal/**")
    }
    repositories {
        jcenter()
        mavenCentral()
    }
}

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2") }
    }
}
