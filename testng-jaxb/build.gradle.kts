import org.gradle.api.tasks.JavaExec

plugins {
    `java-library`
}

val jaxbVersion = "3.0.0"
val jaxb: Configuration by configurations.creating

dependencies {
    jaxb("org.glassfish.jaxb:jaxb-xjc:$jaxbVersion")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:$jaxbVersion")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")
}

val xjcOutputDir = layout.buildDirectory.dir("generated/source/xjc/main")

val xjc = tasks.register("xjc", JavaExec::class) {
    classpath = jaxb
    mainClass.set("com.sun.tools.xjc.XJCFacade")
    args = listOf(
        "-d", xjcOutputDir.get().asFile.absolutePath,
        "-p", "org.testng.xml.jaxb",
        "-encoding", "UTF-8",
        "-no-header",
        "-quiet",
        file("src/main/resources/testng-1.1.xsd").absolutePath
    )
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(xjc)
}

sourceSets {
    main {
        java {
            srcDir(xjcOutputDir)
        }
    }
}
