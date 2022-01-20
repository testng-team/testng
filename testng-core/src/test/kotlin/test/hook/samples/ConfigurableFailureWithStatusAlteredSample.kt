package test.hook.samples

import test.hook.HOOK_INVOKED_ATTRIBUTE

import org.testng.IConfigureCallBack
import org.testng.ITestResult
import org.testng.annotations.Test

class ConfigurableFailureWithStatusAlteredSample : BaseConfigurableSample() {

    override fun run(callBack: IConfigureCallBack, testResult: ITestResult) {
        testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
        // Not calling the callback
        testResult.status = ITestResult.SUCCESS
    }

    @Test
    fun hookWasNotRun() {
    }
}
