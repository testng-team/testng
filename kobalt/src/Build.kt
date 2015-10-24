import com.beust.kobalt.*
import com.beust.kobalt.api.*
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.internal.TaskResult
import com.beust.kobalt.internal.test
import com.beust.kobalt.plugin.java.javaProject
import com.beust.kobalt.plugin.kotlin.kotlinProject
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.kotlin.kotlinCompiler
import com.beust.kobalt.plugin.publish.jcenter

import java.nio.file.*

val GENERATED_DIR = "src/generated/java"

val testng = javaProject {
    name = "testng"
    group = "org.testng"
    artifactId = name
    version = "6.9.9-SNAPSHOT"
    directory = homeDir("java/testng")
    buildDirectory = "kobaltBuild"

    sourceDirectories = arrayListOf("src/main/java", GENERATED_DIR)

    dependencies {
        compile("org.apache.ant:ant:1.7.0",
                "junit:junit:4.10",
                "org.beanshell:bsh:2.0b4",
                "com.google.inject:guice:4.0:no_aop",
                "com.beust:jcommander:1.48",
                "org.yaml:snakeyaml:1.15")
    }
    dependenciesTest {
        compile("org.testng:testng:6.9.8", "org.assertj:assertj-core:3.2.0")
    }
}

val a = assemble(testng) {
    mavenJars {
    }
}

@Task(name = "generateVersionFile", description = "Generate the Version.java file", runBefore = arrayOf("compile"))
fun generateVersionFile(@Suppress("UNUSED_PARAMETER") project: Project) : TaskResult {
    val dirFrom = testng.directory + "/src/main/resources/org/testng/internal/"
    val dirTo = testng.directory + "/$GENERATED_DIR/org/testng/internal/"
    println("Copying version file to $dirTo")
    Files.copy(Paths.get(dirFrom + "VersionTemplateJava"), Paths.get(dirTo + "Version.java"),
            StandardCopyOption.REPLACE_EXISTING)
    return com.beust.kobalt.internal.TaskResult()
}


