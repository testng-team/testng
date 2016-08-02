
import com.beust.kobalt.*
import com.beust.kobalt.api.Project
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.plugin.packaging.assemble
import java.io.File

val VERSION = "6.9.13-SNAPSHOT"

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
                "com.google.inject:guice:4.0",
                "junit:junit:4.10",
                "org.apache.ant:ant:1.7.0",
                "org.beanshell:bsh:2.0b4",
                "org.yaml:snakeyaml:1.15")
    }

    dependenciesTest {
        compile("org.assertj:assertj-core:2.0.0",
                "org.testng:testng:6.9.9",
                "org.spockframework:spock-core:1.0-groovy-2.4")
    }

    test {
        jvmArgs("-Dtest.resources.dir=src/test/resources")
    }

    assemble {
        jar {
            fatJar = true
        }
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
