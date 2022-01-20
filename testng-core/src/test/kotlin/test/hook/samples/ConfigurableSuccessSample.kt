package test.hook.samples

import test.hook.HOOK_INVOKED_ATTRIBUTE
import test.hook.HOOK_METHOD_PARAMS_ATTRIBUTE

import org.testng.IConfigureCallBack
import org.testng.ITestResult
import org.testng.annotations.Test

class ConfigurableSuccessSample : BaseConfigurableSample() {

    override fun run(callBack: IConfigureCallBack, testResult: ITestResult) {
        testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
        testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.parameters)
        callBack.runConfigurationMethod(testResult)
    }

    @Test
    fun hookWasRun() {
    }
}
