package test.hook.samples

import org.testng.IConfigureCallBack
import org.testng.ITestResult
import org.testng.annotations.Test
import test.hook.HOOK_INVOKED_ATTRIBUTE

class ConfigurableFailureSample:BaseConfigurableSample() {

    override fun run(callBack: IConfigureCallBack?, testResult: ITestResult) {
        testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
    }
  @Test
  fun hookWasNotRun() {}
}
