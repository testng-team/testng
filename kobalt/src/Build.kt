
import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.Project
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.plugin.java.javaCompiler
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project
import com.beust.kobalt.repos
import com.beust.kobalt.test
import java.io.File

val VERSION = "6.9.14-SNAPSHOT"

val r = repos("https://dl.bintray.com/cbeust/maven")

//val pl = plugins("com.beust:kobalt-groovy:0.4")
//        file(homeDir("kotlin/kobalt-groovy/kobaltBuild/libs/kobalt-groovy-0.1.jar")))

val p = project {
    name = "testng"
    group = "org.testng"
    artifactId = name
    version = VERSION

    sourceDirectories {
        path("src/generated/java", "src/main/groovy")
    }

    sourceDirectoriesTest {
        path("src/test/groovy")
    }

    dependencies {
        compile("com.beust:jcommander:1.48",
                "com.google.inject:guice:4.1.0",
                "junit:junit:4.12",
                "org.apache.ant:ant:1.9.7",
                "org.beanshell:bsh:2.0b4",
                "org.yaml:snakeyaml:1.17")
    }

    dependenciesTest {
        compile("org.assertj:assertj-core:3.5.2",
                "org.testng:testng:6.9.9",
                "org.spockframework:spock-core:1.0-groovy-2.4")
    }

    test {
        jvmArgs("-Dtest.resources.dir=src/test/resources")
    }

    javaCompiler {
        args("-target", "1.7", "-source", "1.7")
    }

    assemble {
        mavenJars {
        }
    }

    bintray {
        publish = true
    }
}

@Task(name = "createVersion", runBefore = arrayOf("compile"), runAfter = arrayOf("clean"), description = "")
fun taskCreateVersion(project: Project): TaskResult {
    val path = "org/testng/internal"
    with(arrayListOf<String>()) {
        File("src/main/resources/$path/VersionTemplateJava").forEachLine {
            add(it.replace("@version@", VERSION))
        }
        File("src/generated/java/$path/Version.java").writeText(joinToString("\n"))
    }
    return TaskResult()
}
