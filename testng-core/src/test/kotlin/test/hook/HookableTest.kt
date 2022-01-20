package test.hook

import org.assertj.core.api.Assertions.assertThat

import java.lang.reflect.Method
import java.util.UUID
import java.util.function.Consumer
import org.assertj.core.api.SoftAssertions
import org.testng.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import test.SimpleBaseTest
import test.TestData
import test.hook.samples.ConfigurableFailureSample
import test.hook.samples.ConfigurableFailureWithStatusAlteredSample
import test.hook.samples.ConfigurableSuccessSample
import test.hook.samples.ConfigurableSuccessWithListenerSample
import test.hook.samples.HookFailureSample
import test.hook.samples.HookFailureWithStatusAlteredSample
import test.hook.samples.HookSuccessDynamicParametersSample
import test.hook.samples.HookSuccessSample
import test.hook.samples.HookSuccessTimeoutSample
import test.hook.samples.HookSuccessTimeoutWithDataProviderSample
import test.hook.samples.HookSuccessWithListenerSample
import test.hook.samples.issue2251.TestCaseSample
import test.hook.samples.issue2257.TestClassSample
import kotlin.reflect.KClass

const val HOOK_INVOKED_ATTRIBUTE = "hook"
const val HOOK_METHOD_INVOKED_ATTRIBUTE = "hookMethod"
const val HOOK_METHOD_PARAMS_ATTRIBUTE = "hookParamAttribute"

class HookableTest : SimpleBaseTest() {

    @Test(dataProvider = "getTestClasses")
    fun hookSuccess(clazz: KClass<*>, flow: String, assertAttributes: Boolean) {
        Reporter.log("Running scenario $flow", true)
        val listener = TestResultsCollector()
        create(clazz.java).run {
            addListener(listener)
            run()
        }
        assertThat(listener.getPassedMethodNames()).contains("verify")
        if (!assertAttributes) {
            return
        }
        val assertions = SoftAssertions()
        listener.getPassed().forEach {
            assertions.assertThat(it.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull
            assertions.assertThat(it.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNotNull
            if (it.method.isDataDriven) {
                val parameters = it.getAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE) as Array<Any>
                assertions.assertThat(parameters).hasSize(1)
                assertions.assertThat(parameters[0]).isInstanceOf(UUID::class.java)
            }
        }
        assertions.assertAll()
    }

    @DataProvider(name = "getTestClasses")
    fun getTestClasses(): TestData =
        arrayOf(
            arrayOf(HookSuccessSample::class, "Happy Flow", true),
            arrayOf(
                HookSuccessTimeoutWithDataProviderSample::class,
                "DataProvider Test With Timeouts (GITHUB-599)",
                true
            ),
            arrayOf(
                HookSuccessTimeoutSample::class,
                "Regular test With Timeouts (GITHUB-599)",
                true
            ),
            arrayOf(
                HookSuccessDynamicParametersSample::class,
                "With Dynamic Parameters (GITHUB-862)",
                false
            )
        )

    @Test
    fun hookSuccessWithListener() {
        val listener = TestResultsCollector()
        create(HookSuccessWithListenerSample::class.java).run {
            addListener(listener)
            run()
        }
        assertThat(listener.getPassedMethodNames()).contains("verify")
        assertThat(listener.getPassed()[0].getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull
    }

    @Test
    fun hookFailure() {
        val listener = TestResultsCollector()
        create(HookFailureSample::class.java).run {
            addListener(listener)
            run()
        }
        assertThat(listener.getPassedMethodNames()).isEmpty()
        assertThat(listener.getFailedTests()).hasSize(1)
        val failedTestResult = listener.getFailedTests()[0]
        assertThat(failedTestResult.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull
        assertThat(failedTestResult.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNull()
    }

    @Test
    fun hookFailureWithStatusAltered() {
        val listener = TestResultsCollector()
        create(HookFailureWithStatusAlteredSample::class.java).run {
            addListener(listener)
            run()
        }
        assertThat(listener.getPassedMethodNames()).hasSize(1)
        val assertions = SoftAssertions()
        val verifier: Consumer<List<ITestResult>> = Consumer {
            it.forEach { each ->
                assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull
                assertions.assertThat(each.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNull()
            }
        }
        assertions.assertAll()
        verifier.accept(listener.getPassed())
        verifier.accept(listener.getInvoked())
    }

    @Test(dataProvider = "getConfigClasses")
    fun configurableSuccess(clazz: KClass<*>, flow: String) {
        Reporter.log("Running scenario $flow", true)
        val listener = TestResultsCollector()
        create(clazz.java).run {
            addListener(listener)
            run()
        }
        assertThat(listener.getPassedConfigs()).hasSize(4)
        assertThat(listener.getPassedConfigNames()).contains("bs", "bt", "bc", "bm")
        val assertions = SoftAssertions()
        listener.getPassedConfigs()
            .forEach { each ->
                assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull
                val parameters = each.getAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE) as Array<Any>?
                if (parameters != null && parameters.isNotEmpty()) {
                    assertions.assertThat(parameters).hasSize(1)
                    assertions.assertThat(parameters[0]).isInstanceOf(Method::class.java)
                    val method = parameters[0] as Method
                    val methodName = method.name
                    assertions.assertThat(methodName).isEqualTo("hookWasRun")
                }
            }
        assertions.assertAll()
    }

    @DataProvider(name = "getConfigClasses")
    fun getConfigClasses(): TestData = arrayOf(
        arrayOf(ConfigurableSuccessSample::class, "IConfigurable as test class"),
        arrayOf(ConfigurableSuccessWithListenerSample::class, "IConfigurable as listener")
    )

    @Test
    fun configurableFailure() {
        val listener = TestResultsCollector()
        create(ConfigurableFailureSample::class.java).run {
            addListener(listener)
            run()
        }
        assertThat(listener.getPassedConfigNames()).isEmpty()
        assertThat(listener.getFailedConfigs()).hasSize(1)
        val failedConfigResult = listener.getFailedConfigs()[0]
        assertThat(failedConfigResult.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull
        assertThat(failedConfigResult.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNull()
        assertThat(listener.getSkippedConfigs()).hasSize(3)
        val assertions = SoftAssertions()
        listener.getSkippedConfigs()
            .forEach { each ->
                assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNull()
                assertions.assertThat(each.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNull()
            }
        assertions.assertAll()
    }

    @Test
    fun configurableFailureWithStatusAltered() {
        val listener = TestResultsCollector()
        create(ConfigurableFailureWithStatusAlteredSample::class.java).run {
            addListener(listener)
            run()
        }
        assertThat(listener.getPassedConfigNames()).containsExactly("bs", "bt", "bc", "bm")
        assertThat(listener.getPassedConfigs()).hasSize(4)
        val assertions = SoftAssertions()
        listener.getPassedConfigs()
            .forEach { each ->
                assertions.assertThat(each.getAttribute(HOOK_INVOKED_ATTRIBUTE)).isNotNull
                assertions.assertThat(each.getAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE)).isNull()

            }
        assertions.assertAll()
    }

    @Test(description = "GITHUB-2257")
    fun ensureConfigurationsCanBeRetriedViaCallBacks() {
        val listener = TestListenerAdapter()
        val testng = create(TestClassSample::class.java).let {
            it.addListener(listener)
            it.run()
            it
        }
        assertThat(listener.configurationSkips).isEmpty()
        assertThat(listener.configurationFailures).isEmpty()
        assertThat(testng.status).isEqualTo(0)
    }

    @Test(description = "GITHUB-2266")
    fun ensureTestsCanBeRetriedViaCallBacks() {
        val listener = TestListenerAdapter()
        val testng = create(test.hook.samples.issue2266.TestClassSample::class.java).let {
            it.addListener(listener)
            it.run()
            it
        }
        assertThat(listener.failedTests).isEmpty()
        assertThat(listener.skippedTests).isEmpty()
        assertThat(listener.passedTests).hasSize(1)
        assertThat(testng.status).isEqualTo(0)
    }

    @Test(description = "GITHUB-2251")
    fun ensureTimeoutsAreHandled() {
        val exception = create(TestCaseSample::class.java).run {
            val l = TestListenerAdapter()
            addListener(l)
            run()
            l.failedTests[0].throwable
        }
        assertThat(exception).isInstanceOf(NullPointerException::class.java)
    }

    class TestResultsCollector : IInvokedMethodListener {
        private val passed = mutableListOf<ITestResult>()
        private val invoked = mutableListOf<ITestResult>()
        private val passedConfigs = mutableListOf<ITestResult>()

        override fun afterInvocation(method: IInvokedMethod, testResult: ITestResult) {
            invoked.add(testResult)
            if (testResult.isSuccess) {
                if (method.isTestMethod) {
                    passed.add(testResult)
                }
                if (method.isConfigurationMethod) {
                    passedConfigs.add(testResult)
                }
            }
        }

        fun getPassedConfigs() = passedConfigs

        fun getFailedConfigs() = invoked.filter {
            it.status == ITestResult.FAILURE
        }.toList()

        fun getSkippedConfigs() = invoked.filter {
            !it.method.isTest
        }.filter {
            it.status == ITestResult.SKIP
        }.toList()

        fun getPassed() = passed

        fun getInvoked() = invoked

        fun getFailedTests() = invoked.filter {
            it.method.isTest
        }.filter {
            it.status == ITestResult.FAILURE
        }.toList()

        fun getPassedMethodNames() = asString(passed)

        fun getPassedConfigNames() = asString(passedConfigs)

        private fun asString(list: List<ITestResult>) =
            list.map {
                it.method.methodName
            }.toList()
    }
}
