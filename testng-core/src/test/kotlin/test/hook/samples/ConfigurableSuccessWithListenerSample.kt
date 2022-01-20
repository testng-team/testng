package test.hook.samples

import test.hook.HOOK_INVOKED_ATTRIBUTE
import test.hook.HOOK_METHOD_PARAMS_ATTRIBUTE

import java.lang.reflect.Method
import org.testng.IConfigurable
import org.testng.IConfigureCallBack
import org.testng.ITestResult
import org.testng.annotations.*

@Listeners(ConfigurableSuccessWithListenerSample.ConfigurableListener::class)
class ConfigurableSuccessWithListenerSample {

    @BeforeSuite
    fun bs() {
    }

    @BeforeMethod
    fun bt() {
    }

    @BeforeMethod
    fun bm(m: Method) {
    }

    @BeforeClass
    fun bc() {
    }

    @Test
    fun hookWasRun() {
    }

    class ConfigurableListener : IConfigurable {

        override fun run(callBack: IConfigureCallBack, testResult: ITestResult) {
            testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
            testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.parameters)
            callBack.runConfigurationMethod(testResult)
        }
    }
}

