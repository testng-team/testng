plugins {
    id("testng.java-library")
    id("com.intershop.gradle.jaxb") version "6.0.0"
}

dependencies {
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    api(projects.testngCoreApi)
}

jaxb {
    javaGen {
        register("testng") {
            schema = file("src/main/resources/testng-1.1.xsd")
            packageName = "org.testng.xml.jaxb"
        }
    }
}

sourceSets {
    main {
        java {
            srcDir(tasks.named("jaxbJavaGenTestng"))
        }
    }
}
