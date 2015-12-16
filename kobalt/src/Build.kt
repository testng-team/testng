
import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.Project
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.plugin.java.javaProject
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugins
import com.beust.kobalt.test
import java.io.File

//import com.beust.kobalt.plugin.linecount.*

val VERSION = "6.9.10-SNAPSHOT"

//val plugins = plugins("com.beust.kobalt:kobalt-line-count:0.17")

val p = javaProject {

//    line
    name = "testng"
    group = "org.testng"
    artifactId = name
    version = VERSION

    sourceDirectories {
        path("src/generated/java")
    }

    dependencies {
        compile("com.beust:jcommander:1.48",
                "com.google.inject:guice:4.0",
                "junit:junit:4.10",
                "org.apache.ant:ant:1.7.0",
                "org.beanshell:bsh:2.0b4",
                "org.yaml:snakeyaml:1.15")
    }

    dependenciesTest {
        compile("org.assertj:assertj-core:2.0.0",
                "org.testng:testng:6.9.9")
    }

    test {
        jvmArgs("-Dtest.resources.dir=src/test/resources")
    }

    assemble {
        jar {
        }
    }
}

@Task(name = "createVersion", runBefore = arrayOf("compile"), runAfter = arrayOf("clean"), description = "")
fun taskCreateVersion(project: Project) : TaskResult {
    val path = "org/testng/internal"
    with(arrayListOf<String>()) {
        File("src/main/resources/$path/VersionTemplateJava").forEachLine {
            add(it.replace("@version@", VERSION))
        }
        File("src/generated/java/$path/Version.java").writeText(joinToString("\n"))
    }
    return TaskResult()
}

