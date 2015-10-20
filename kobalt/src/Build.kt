import com.beust.kobalt.*
import com.beust.kobalt.internal.test
import com.beust.kobalt.plugin.java.javaProject
import com.beust.kobalt.plugin.kotlin.kotlinProject
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.kotlin.kotlinCompiler
import com.beust.kobalt.plugin.publish.jcenter
//import com.beust.kobalt.plugin.linecount.lineCount
//val plugins = plugins(
//        "com.beust.kobalt:kobalt-line-count:0.15"
////        file(homeDir("kotlin/kobalt-line-count/kobaltBuild/libs/kobalt-line-count-0.14.jar"))
//)
//
//val lc = lineCount {
//    suffix = "**.md"
//}

fun readVersion() : String {
    val p = java.util.Properties()
    p.load(java.io.FileReader(java.io.File("src/main/resources/kobalt.properties")))
    return p.getProperty("kobalt.version")
}

val wrapper = javaProject {
    name = "kobalt-wrapper"
    version = readVersion()
    directory = homeDir("kotlin/kobalt/modules/wrapper")
}

val assembleWrapper = assemble(wrapper) {
    jar {
        name = wrapper.name + ".jar"
        manifest {
            attributes("Main-Class", "com.beust.kobalt.wrapper.Main")
        }
    }
}
val kobalt = kotlinProject(wrapper) {
    name = "kobalt"
    group = "com.beust"
    artifactId = name
    version = readVersion()
    description = "A build system in Kotlin"
    url = "http://beust.com/kobalt"
    licenses = listOf(com.beust.kobalt.api.License("Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0"))
    scm = com.beust.kobalt.api.Scm(
            url = "http://github.com/cbeust/kobalt",
            connection = "https://github.com/cbeust/kobalt.git",
            developerConnection = "git@github.com:cbeust/kobalt.git")

    dependenciesTest {
        compile("org.testng:testng:6.9.6")
    }

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-stdlib:0.14.449",
                "org.jetbrains.kotlin:kotlin-compiler-embeddable:0.14.449",

//                file(homeDir("java/jcommander/target/jcommander-1.47.jar")),
                "com.beust:jcommander:1.48",
                "com.beust:klaxon:0.16",
                "com.squareup.okhttp:okhttp:2.5.0",
                "org.jsoup:jsoup:1.8.3",
                "com.google.inject:guice:4.0",
                "com.google.inject.extensions:guice-assistedinject:4.0",
                "com.google.guava:guava:19.0-rc2",
                "org.apache.maven:maven-model:3.3.3",
                "com.github.spullara.mustache.java:compiler:0.9.1",
                "io.reactivex:rxjava:1.0.14",
                "com.google.code.gson:gson:2.4"
              )
    }
}

val testKobalt = test(kobalt) {
    args("-log", "2", "src/test/resources/testng.xml")
}

val assembleKobalt = assemble(kobalt) {
    mavenJars {
        fatJar = true
        manifest {
            attributes("Main-Class", "com.beust.kobalt.KobaltPackage")
        }
    }
    zip {
        include("kobaltw")
        include(from("${kobalt.buildDirectory}/libs"), to("kobalt/wrapper"),
                "${kobalt.name}-${kobalt.version}.jar")
        include(from("modules/wrapper/${kobalt.buildDirectory}/libs"), to("kobalt/wrapper"),
                "${kobalt.name}-wrapper.jar")
    }
}

val cs = kotlinCompiler {
    args("-nowarn")
}


val jc = jcenter(kobalt) {
    publish = true
    file("${kobalt.buildDirectory}/libs/${kobalt.name}-${kobalt.version}.zip",
            "${kobalt.name}/${kobalt.version}/${kobalt.name}-${kobalt.version}.zip")
}

//val testng = javaProject {
//    name = "testng"
//    group = "org.testng"
//    artifactId = name
//    version = "6.9.6-SNAPSHOT"
//    directory = homeDir("java/testng")
//    buildDirectory = "kobaltBuild"
//
//    sourceDirectoriesTest {
//        path("src/test/java")
//        path("src/test/resources")
//    }
//    sourceDirectories {
//        path("src/main/java")
//        path("src/generated/java")
//    }
//    dependencies {
//        compile("org.apache.ant:ant:1.7.0",
//                "junit:junit:4.10",
//                "org.beanshell:bsh:2.0b4",
//                "com.google.inject:guice:4.0:no_aop",
//                "com.beust:jcommander:1.48",
//                "org.yaml:snakeyaml:1.15")
//    }
//}
//
//@Task(name = "generateVersionFile", description = "Generate the Version.java file", runBefore = arrayOf("compile"))
//fun createVersionFile(project: Project) : com.beust.kobalt.internal.TaskResult {
//    val dirFrom = testng.directory + "/src/main/resources/org/testng/internal/"
//    val dirTo = testng.directory + "/src/generated/java/org/testng/internal/"
//    println("COPYING VERSION FILE")
//    Files.copy(Paths.get(dirFrom + "VersionTemplateJava"), Paths.get(dirTo + "Version.java"),
//            StandardCopyOption.REPLACE_EXISTING)
//    return com.beust.kobalt.internal.TaskResult()
//}
//
