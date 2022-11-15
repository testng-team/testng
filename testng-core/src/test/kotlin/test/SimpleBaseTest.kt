package test

import org.assertj.core.api.Assertions.assertThat
import org.testng.*
import org.testng.annotations.ITestAnnotation
import org.testng.internal.annotations.AnnotationHelper
import org.testng.internal.annotations.DefaultAnnotationTransformer
import org.testng.internal.annotations.IAnnotationFinder
import org.testng.internal.annotations.JDK15AnnotationFinder
import org.testng.xml.*
import org.testng.xml.internal.Parser
import java.io.*
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import java.util.stream.Collectors

open class SimpleBaseTest {

    companion object {
        private const val TEST_RESOURCES_DIR = "test.resources.dir"

        @JvmStatic
        fun run(vararg testClasses: Class<*>) = run(false, *testClasses)

        @JvmStatic
        fun run(skipConfiguration: Boolean, tng: TestNG) =
            InvokedMethodNameListener(skipConfiguration).apply {
                tng.addListener(this)
                tng.run()
            }

        @JvmStatic
        fun run(skipConfiguration: Boolean, vararg testClasses: Class<*>) =
            run(skipConfiguration, create(*testClasses))

        @JvmStatic
        fun create(vararg testClasses: Class<*>) = create().apply {
            setTestClasses(testClasses)
        }

        @JvmStatic
        fun create() = TestNG().apply {
            setUseDefaultListeners(false)
        }

        @JvmStatic
        fun create(xmlSuite: XmlSuite) = create().apply {
            setXmlSuites(listOf(xmlSuite))
        }

        @JvmStatic
        fun run(vararg suites: XmlSuite): InvokedMethodNameListener = run(false, *suites)

        @JvmStatic
        fun run(skipConfiguration: Boolean, vararg suites: XmlSuite) =
            run(skipConfiguration, create(*suites))

        @JvmStatic
        protected fun create(outputDir: Path, vararg testClasses: Class<*>) =
            create(*testClasses).apply {
                outputDirectory = outputDir.toAbsolutePath().toString()
            }

        @JvmStatic
        protected fun create(vararg suites: XmlSuite) = create(listOf(*suites))

        @JvmStatic
        protected fun create(suites: List<XmlSuite>) = create().apply {
            setXmlSuites(suites)
        }

        @JvmStatic
        protected fun create(outputDir: Path, vararg suites: XmlSuite) =
            create(outputDir, listOf(*suites))

        @JvmStatic
        protected fun create(outputDir: Path, suites: List<XmlSuite>) = create(suites).apply {
            outputDirectory = outputDir.toAbsolutePath().toString()
        }

        @JvmStatic
        protected fun createTests(suiteName: String, vararg testClasses: Class<*>) =
            createTests(null, suiteName, *testClasses)

        @JvmStatic
        protected fun createTests(
            outDir: Path?,
            suiteName: String,
            vararg testClasses: Class<*>
        ) = createXmlSuite(suiteName).let { suite ->
            for ((i, testClass) in testClasses.withIndex()) {
                createXmlTest(suite, testClass.name + i, testClass)
            }
            //if outDir is not null then create suite with outDir, else create suite without it.
            outDir?.let { create(it, suite) } ?: create(suite)
        }

        @JvmStatic
        protected fun createDummySuiteWithTestNamesAs(vararg tests: String) =
            XmlSuite().apply {
                name = "random_suite"
                tests.forEach {
                    XmlTest(this).apply {
                        name = it
                    }
                }
            }

        @JvmStatic
        protected fun createXmlSuite(name: String) = XmlSuite().apply {
            this.name = name
        }

        @JvmStatic
        protected fun createXmlSuite(params: Map<String, String>) =
            createXmlSuite(UUID.randomUUID().toString()).apply {
                parameters = params
            }

        @JvmStatic
        protected fun createXmlSuite(
            suiteName: String,
            testName: String,
            vararg classes: Class<*>
        ) = createXmlSuite(suiteName).apply {
            createXmlTest(this, testName, *classes)
        }

        @JvmStatic
        protected fun createXmlSuite(suiteName: String, params: Map<String, String>) =
            createXmlSuite(suiteName).apply {
                parameters = params
            }

        @JvmStatic
        protected fun createXmlTestWithPackages(
            suite: XmlSuite, name: String, vararg packageName: String
        ) = createXmlTest(suite, name).apply {
            packages = packageName.map {
                XmlPackage().apply {
                    this.name = it
                }
            }.toMutableList()
        }

        @JvmStatic
        protected fun createXmlTestWithPackages(
            suite: XmlSuite, name: String, vararg packageName: Class<*>
        ) = createXmlTest(suite, name).apply {
            packages = packageName.map {
                XmlPackage().apply {
                    this.name = it.`package`.name
                }
            }.toMutableList()
        }

        @JvmStatic
        protected fun createXmlTest(suiteName: String, testName: String) =
            createXmlTest(createXmlSuite(suiteName), testName)

        @JvmStatic
        protected fun createXmlTest(
            suiteName: String,
            testName: String,
            vararg classes: Class<*>
        ) = createXmlTest(createXmlSuite(suiteName), testName).apply {
            classes.forEach {
                xmlClasses.add(XmlClass(it))
            }
        }

        @JvmStatic
        protected fun createXmlTest(suite: XmlSuite, name: String) = XmlTest(suite).apply {
            this.name = name
        }

        @JvmStatic
        protected fun createXmlTest(
            suite: XmlSuite,
            name: String,
            params: Map<String, String>
        ) = XmlTest(suite).apply {
            this.name = name
            setParameters(params)
        }

        @JvmStatic
        protected fun createXmlTest(
            suite: XmlSuite,
            name: String,
            vararg classes: Class<*>
        ) = createXmlTest(suite, name).apply {
            for ((index, c) in classes.withIndex()) {
                val xc = XmlClass(c.name, index, true /* load classes */)
                xmlClasses.add(xc)
            }
        }

        @JvmStatic
        protected fun createXmlClass(test: XmlTest, testClass: Class<*>) =
            XmlClass(testClass).apply {
                test.xmlClasses.add(this)
            }

        @JvmStatic
        protected fun createXmlClass(
            test: XmlTest, testClass: Class<*>, params: Map<String, String>
        ) = createXmlClass(test, testClass).apply {
            setParameters(params)
        }

        @JvmStatic
        protected fun createXmlInclude(clazz: XmlClass, method: String) = XmlInclude(method).apply {
            setXmlClass(clazz)
            clazz.includedMethods.add(this)
        }

        @JvmStatic
        protected fun createXmlInclude(
            clazz: XmlClass, method: String, params: Map<String, String>
        ) = createXmlInclude(clazz, method).apply {
            setParameters(params)
        }

        @JvmStatic
        protected fun createXmlInclude(
            clazz: XmlClass,
            method: String,
            index: Int, vararg list: Int
        ) = XmlInclude(method, list.asList(), index).apply {
            setXmlClass(clazz)
            clazz.includedMethods.add(this)
        }

        @JvmStatic
        protected fun createXmlGroups(
            suite: XmlSuite,
            vararg includedGroupNames: String
        ) = createGroupIncluding(*includedGroupNames).apply {
            suite.groups = this
        }

        @JvmStatic
        protected fun createXmlGroups(
            test: XmlTest,
            vararg includedGroupNames: String
        ) = createGroupIncluding(*includedGroupNames).apply {
            test.setGroups(this)
        }

        @JvmStatic
        private fun createGroupIncluding(vararg groupNames: String) = XmlGroups().apply {
            run = XmlRun().apply {
                groupNames.forEach { onInclude(it) }
            }
        }

        @JvmStatic
        protected fun createXmlTest(
            suite: XmlSuite,
            name: String,
            vararg classes: String
        ) = createXmlTest(suite, name).apply {
            for ((index, c) in classes.withIndex()) {
                val xc = XmlClass(c, index, true /* load classes */)
                xmlClasses.add(xc)
            }
        }

        @JvmStatic
        protected fun addMethods(cls: XmlClass, vararg methods: String) {
            for ((index, method) in methods.withIndex()) {
                cls.includedMethods.add(XmlInclude(method, index))
            }
        }

        @JvmStatic
        fun getPathToResource(fileName: String): String {
            val result = System.getProperty(TEST_RESOURCES_DIR, "src/test/resources")
                ?: throw IllegalArgumentException(
                    "System property $TEST_RESOURCES_DIR was not defined."
                )
            return result + File.separatorChar + fileName
        }

        @JvmStatic
        fun extractTestNGMethods(vararg classes: Class<*>): List<ITestNGMethod> =
            XmlSuite().let { xmlSuite ->
                xmlSuite.name = "suite"
                val xmlTest = createXmlTest(xmlSuite, "tests", *classes)
                val annotationFinder: IAnnotationFinder =
                    JDK15AnnotationFinder(DefaultAnnotationTransformer())
                classes.flatMap { clazz ->
                    AnnotationHelper.findMethodsWithAnnotation(
                        object : ITestObjectFactory {},
                        clazz,
                        ITestAnnotation::class.java,
                        annotationFinder,
                        xmlTest
                    ).toMutableList()
                }
            }

        /** Compare a list of ITestResult with a list of String method names,  */
        @JvmStatic
        protected fun assertTestResultsEqual(results: List<ITestResult>, methods: List<String>) {
            results.map { it.method.methodName }
                .toList()
                .run {
                    assertThat(this).containsAll(methods)
                }
        }

        /** Deletes all files and subdirectories under dir.  */
        @JvmStatic
        protected fun deleteDir(dir: File): Path =
            Files.walkFileTree(dir.toPath(), TestNGFileVisitor())

        @JvmStatic
        protected fun createDirInTempDir(dir: String): File =
            Files.createTempDirectory(dir)
                .toFile().apply {
                    deleteOnExit()
                }

        /**
         * @param fileName The filename to parse
         * @param regexp The regular expression
         * @param resultLines An out parameter that will contain all the lines that matched the regexp
         * @return A List<Integer> containing the lines of all the matches
         *
         * Note that the size() of the returned valuewill always be equal to result.size() at the
         * end of this function.
        </Integer> */
        @JvmStatic
        protected fun grep(
            fileName: File,
            regexp: String,
            resultLines: MutableList<String>
        ) = grep(FileReader(fileName), regexp, resultLines)

        @JvmStatic
        protected fun grep(
            reader: Reader,
            regexp: String,
            resultLines: MutableList<String>
        ): List<Int> {
            val resultLineNumbers = mutableListOf<Int>()
            val p = Pattern.compile(".*$regexp.*")
            val counter = AtomicInteger(-1)
            BufferedReader(reader).lines()
                .peek { counter.getAndIncrement() }
                .filter { line -> p.matcher(line).matches() }
                .forEach {
                    resultLines.add(it)
                    resultLineNumbers.add(counter.get())
                }
            return resultLineNumbers
        }

        @JvmStatic
        protected fun getSuites(vararg suiteFiles: String) = suiteFiles.map { Parser(it) }
            .flatMap { it.parseToList() }
            .toList()

        @JvmStatic
        protected fun getFailedResultMessage(testResultList: List<ITestResult>): String {
            val methods = testResultList.stream()
                .map { r: ITestResult ->
                    AbstractMap.SimpleEntry(
                        r.method.qualifiedName, r.throwable
                    )
                }
                .map { (key, value): AbstractMap.SimpleEntry<String?, Throwable?> ->
                    "$key: $value"
                }
                .collect(Collectors.joining("\n"))
            return "Failed methods should pass: \n $methods"
        }
    }

    class TestNGFileVisitor : SimpleFileVisitor<Path>() {

        @Throws(IOException::class)
        override fun postVisitDirectory(
            dir: Path,
            exc: IOException?
        ): FileVisitResult {
            dir.toFile().deleteRecursively()
            return FileVisitResult.CONTINUE
        }
    }
}
