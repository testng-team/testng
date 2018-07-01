
import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.Project
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.plugin.java.javaCompiler
import com.beust.kobalt.plugin.osgi.osgi
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.autoGitTag
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project
import com.beust.kobalt.test
import org.apache.maven.model.Developer
import org.apache.maven.model.License
import org.apache.maven.model.Model
import org.apache.maven.model.Scm
import java.io.File

val VERSION = "7.0.0-SNAPSHOT"

val p = project {
    name = "testng"
    group = "org.testng"
    artifactId = name
    url = "http://testng.org"
    version = VERSION

    pom = Model().apply {
        name = project.name
        description = "A testing framework for the JVM"
        url = "http://testng.org"
        licenses = listOf(License().apply {
            name = "Apache 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0"
        })
        scm = Scm().apply {
            url = "http://github.com/cbeust/testng"
            connection = "scm:git:https://github.com/cbeust/testng.git"
            developerConnection = "scm:git:git@github.com:cbeust/testng.git"
        }
        developers = listOf(Developer().apply {
            name = "Cedric Beust"
            email = "cedric@beust.com"
        })
    }

    sourceDirectories {
        path("src/generated/java", "src/main/groovy")
    }

    sourceDirectoriesTest {
        path("src/test/groovy")
    }

    dependencies {
        provided("com.google.inject:guice:4.1.0",
                "com.google.code.findbugs:jsr305:3.0.1"
                )
        compile("com.beust:jcommander:1.72"
                )
        provided("org.yaml:snakeyaml:1.17",
                "org.apache.ant:ant:1.9.7",
                "junit:junit:4.12")
    }

    dependenciesTest {
        compile("org.assertj:assertj-core:3.10.0",
                "org.testng:testng:6.9.13.7",
                "org.mockito:mockito-core:2.12.0",
                "org.codehaus.groovy:groovy-all:2.4.7",
                "org.spockframework:spock-core:1.0-groovy-2.4",
                "org.apache-extras.beanshell:bsh:2.0b6",
                "org.jboss.shrinkwrap:shrinkwrap-api:1.2.6",
                "org.jboss.shrinkwrap:shrinkwrap-impl-base:1.2.6")
    }

    test {
        jvmArgs("-Dtest.resources.dir=src/test/resources")
    }

    javaCompiler {
        args("-target", "1.8", "-source", "1.8", "-encoding", "UTF-8")
    }

    assemble {
        mavenJars {
        }
    }

    bintray {
        publish = true
        sign = true
    }

    autoGitTag {
        enabled = true
    }

    osgi{}
}

@Task(name = "createVersion", reverseDependsOn = arrayOf("compile"), runAfter = arrayOf("clean"), description = "")
fun taskCreateVersion(project: Project): TaskResult {
    val path = "org/testng/internal"
    with(arrayListOf<String>()) {
        File("src/main/resources/$path/VersionTemplateJava").forEachLine {
            add(it.replace("@version@", VERSION))
        }
        File("src/generated/java/$path").apply {
            mkdirs()
            File(this, "Version.java").apply {
                writeText(joinToString("\n"))
            }
        }
    }
    return TaskResult()
}
