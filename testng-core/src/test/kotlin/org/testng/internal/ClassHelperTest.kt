package org.testng.internal

import org.assertj.core.api.Assertions.assertThat
import org.testng.Reporter

import java.net.URL
import java.net.URLClassLoader
import java.util.UUID
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.testng.internal.samples.classhelper.issue1339.BabyPandaSample
import org.testng.internal.samples.classhelper.issue1339.LittlePandaSample
import org.testng.internal.samples.classhelper.issue1456.TestClassSample
import org.testng.internal.samples.classhelper.misamples.AbstractMovesSample
import org.testng.internal.samples.classhelper.misamples.BatmanSample
import org.testng.internal.samples.classhelper.misamples.JohnTravoltaMovesSample
import org.testng.internal.samples.classhelper.misamples.MickJaggerSample
import org.testng.xml.XmlClass
import org.testng.xml.XmlSuite
import org.testng.xml.XmlTest
import kotlin.reflect.KClass

typealias TestData = Array<Array<Any>>

class ClassHelperTest {

    private val defaultMethods = listOf("announcer", "announcer", "inheritable", "inheritable")

    @Test(dataProvider = "testDataProvider")
    fun runFindClassesInSameTestScenarios(
        scenarioName: String,//This parameter will help us differentiate scenarios in reports
        classToBeFound: KClass<*>,
        expectedCount: Int, vararg classes: KClass<*>,
    ) {
        Reporter.log("Running Scenario $scenarioName")
        val xmlSuite = XmlSuite().apply {
            name = "xml_suite"
        }
        listOf("test1", "test2", "test3").forEach {
            xmlSuite.addXmlTest(it, *classes)
        }
        val xmlClasses = ClassHelper.findClassesInSameTest(classToBeFound.java, xmlSuite)
        assertThat(xmlClasses).hasSize(expectedCount)
    }

    @DataProvider(name = "testDataProvider")
    fun testDataProvider(): TestData {
        return arrayOf(
            arrayOf("FindClassInSameTest", TestClassSample::class, 1, TestClassSample::class),
            arrayOf(
                "FindClassesInSameTest",
                TestClassSample::class, 2, TestClassSample::class, BabyPandaSample::class
            ),
        )
    }

    @Test(dataProvider = "scenariosData")
    fun runAvailableMethodsScenarios(
        scenarioName: String,//This parameter will help us differentiate scenarios in reports
        expected: List<String>,
        whichClass: KClass<*>
    ) {
        Reporter.log("Running Scenario $scenarioName")
        val actual = ClassHelper.getAvailableMethods(whichClass.java)
            .filter {
                //jacocoInit() is something that Gradle adds. So filter it out.
                !"\$jacocoInit".contentEquals(it.name)
            }
            .map {
                it.name
            }.toList()
        assertThat(actual).containsAll(expected)
    }

    @DataProvider(name = "scenariosData")
    fun scenariosData(): TestData {
        return arrayOf(
            arrayOf("AllAvailableMethods", defaultMethods, LittlePandaSample::class),
            arrayOf(
                "AvailableMethodsWhenOverridingIsInvolved",
                defaultMethods + listOf("equals", "hashCode", "toString"),
                BabyPandaSample::class
            ),
        )
    }

    @Test
    fun testNoClassDefFoundError() {
        val urlClassLoader = object : URLClassLoader(emptyArray<URL>()) {
            override fun loadClass(className: String?): Class<*> {
                throw NoClassDefFoundError()
            }
        }
        ClassHelper.addClassLoader(urlClassLoader)
        val fakeClassName = UUID.randomUUID().toString()
        assertThat(ClassHelper.forName(fakeClassName))
            .withFailMessage("The result should be null; no exception should be thrown.")
            .isNull()
    }

    @Test(dataProvider = "data")
    fun testWithDefaultMethodsBeingOverridden(
        cls: KClass<*>, expectedCount: Int, vararg expected: String
    ) {
        val methods = ClassHelper.getAvailableMethodsExcludingDefaults(cls.java)
            .map {
                it.declaringClass.name + "." + it.name
            }
        assertThat(methods).hasSize(expectedCount)
        assertThat(methods).contains(*expected)
    }

    @DataProvider(name = "data")
    fun getTestData(): TestData {
        return arrayOf(
            arrayOf(MickJaggerSample::class, 1, MickJaggerSample::class.java.name + ".dance"),
            arrayOf(
                JohnTravoltaMovesSample::class,
                2,
                JohnTravoltaMovesSample::class.java.name + ".walk",
                AbstractMovesSample::class.java.name + ".dance"
            ),
            arrayOf(
                BatmanSample::class,
                3,
                BatmanSample::class.java.name + ".fly",
                BatmanSample::class.java.name + ".liftWeights",
                BatmanSample::class.java.name + ".yellSlogan"
            )
        )
    }

    private fun XmlSuite.addXmlTest(testName: String, vararg clazz: KClass<*>): XmlSuite {
        XmlTest(this).apply {
            name = testName
            xmlClasses = newXmlClasses(*clazz)
        }
        return this
    }

    private fun newXmlClasses(vararg classes: KClass<*>): List<XmlClass> {
        return classes.map {
            XmlClass(it.java)
        }.toList()
    }
}
