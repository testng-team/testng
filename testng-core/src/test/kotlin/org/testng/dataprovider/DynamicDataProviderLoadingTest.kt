package org.testng.dataprovider

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.netbeans.lib.profiler.heap.HeapFactory2
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass
import org.testng.Reporter
import org.testng.annotations.Test
import org.testng.dataprovider.sample.issue2724.*
import test.SimpleBaseTest
import java.io.File
import java.nio.file.Files

const val CLASS_NAME_DP = "org.testng.dataprovider.sample.issue2724.DataProviders"
const val CLASS_NAME_DP_LOADER = "org.testng.internal.DataProviderLoader"

class DynamicDataProviderLoadingTest : SimpleBaseTest() {

    @Test
    fun testDynamicDataProviderPasses() {
        val listener = run(SampleDynamicDP::class.java)
        assertThat(listener.failedMethodNames).isEmpty()
        assertThat(listener.succeedMethodNames).containsExactly(
            "testDynamicDataProvider(Mike,34,student)",
            "testDynamicDataProvider(Mike,23,driver)",
            "testDynamicDataProvider(Paul,20,director)",
        )
        assertThat(listener.skippedMethodNames).isEmpty()
    }

    @Test
    fun testDynamicDataProviderUnloaded() {
        val tempDirectory = Files.createTempDirectory("temp-testng-")
        val dumpPath = "%s/%s".format(tempDirectory.toAbsolutePath().toString(), "dump.hprof")
        val dumpPathBeforeSample =
            "%s/%s".format(tempDirectory.toAbsolutePath().toString(), "dump-before-sample.hprof")
        System.setProperty("memdump.path", dumpPath)

        saveMemDump(dumpPathBeforeSample)
        val heapDumpBeforeSampleFile = File(dumpPathBeforeSample)
        assertThat(heapDumpBeforeSampleFile).exists()
        var heap = HeapFactory2.createHeap(heapDumpBeforeSampleFile, null)
        val beforeSampleDPClassDump: JavaClass? = heap.getJavaClassByName(CLASS_NAME_DP)
        assertThat(beforeSampleDPClassDump)
            .describedAs(
                "Class $CLASS_NAME_DP shouldn't be loaded, before test sample started. "
            )
            .isNull()

        run(SampleDPUnloaded::class.java)

        val heapDumpFile = File(dumpPath)
        assertThat(heapDumpFile).exists()
        heap = HeapFactory2.createHeap(heapDumpFile, null)

        with(SoftAssertions()) {
            val dpLoaderClassDump: JavaClass? = heap.getJavaClassByName(CLASS_NAME_DP_LOADER)
            val dpClassDump: JavaClass? = heap.getJavaClassByName(CLASS_NAME_DP)
            val dpLoaderMessage = dpLoaderClassDump?.instances?.joinToString("\n") {
                getGCPath(it)
            }
            val dpMessage = dpLoaderClassDump?.instances?.joinToString("\n") {
                getGCPath(it)
            }

            this.assertThat(dpLoaderClassDump?.instances)
                .describedAs(
                    """
                    All instances of class $CLASS_NAME_DP_LOADER should be garbage collected, but was not.
                    Path to GC root is:
                    $dpLoaderMessage
                    """.trimIndent()
                )
                .isEmpty()
            this.assertThat(dpClassDump)
                .describedAs(
                    """
                    Class $CLASS_NAME_DP shouldn't be loaded, but it was.
                    Path to GC root is:
                    $dpMessage
                    """.trimIndent()
                )
                .isNull()
            this.assertAll()
        }
    }

    @Test
    fun comparePerformanceAgainstCsvFiles() {
        val simpleDPSuite = create().apply {
            setTestClasses(arrayOf(SampleSimpleDP::class.java))
            setListenerClasses(listOf(TestTimeListener::class.java))
        }
        val csvSuite = create().apply {
            setTestClasses(arrayOf(SampleWithCSVData::class.java))
            setListenerClasses(listOf(TestTimeListener::class.java))
        }
        val dataAsCodeSuite = create().apply {
            setTestClasses(arrayOf(SampleDynamicDP::class.java))
            setListenerClasses(listOf(TestTimeListener::class.java))
        }

        Reporter.log("Test execution time:\n")
        for (suite in listOf(
            Pair("simple dataprovider", simpleDPSuite),
            Pair("dataprovider as code", dataAsCodeSuite),
            Pair("csv dataprovider", csvSuite),
        )) {
            run(false, suite.second)
            Reporter.log(
                "${suite.first} execution times: %d milliseconds."
                    .format(TestTimeListener.testRunTime),
                true
            )
        }
    }

    fun getGCPath(instance: Instance): String {
        var result = ""
        if (!instance.isGCRoot) {
            result += getGCPath(instance.nearestGCRootPointer)
        }
        return result + "${instance.javaClass.name}\n"
    }
}
