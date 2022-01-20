package test.hook.samples

import test.hook.*

import org.testng.Assert
import org.testng.IHookCallBack
import org.testng.IHookable
import org.testng.ITestResult
import org.testng.Reporter
import org.testng.annotations.Listeners
import org.testng.annotations.Test

@Listeners(HookSuccessWithListenerSample.HookListener::class)
class HookSuccessWithListenerSample {

    @Test
    fun verify() {
        val itr = Reporter.getCurrentTestResult()
        val attribute = itr.getAttribute(HOOK_INVOKED_ATTRIBUTE).toString().toBoolean()
        Assert.assertTrue(attribute)
    }

    class HookListener : IHookable {
        override fun run(callBack: IHookCallBack, testResult: ITestResult) {
            testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
            callBack.runTestMethod(testResult)
        }
    }
}
