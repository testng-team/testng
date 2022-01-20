package test.hook.samples

import test.hook.HOOK_INVOKED_ATTRIBUTE
import test.hook.HOOK_METHOD_INVOKED_ATTRIBUTE

import org.testng.IHookCallBack
import org.testng.IHookable
import org.testng.ITestResult
import org.testng.Reporter
import org.testng.annotations.Test

class HookFailureWithStatusAlteredSample : IHookable {

    override fun run(callBack: IHookCallBack, testResult: ITestResult) {
        testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
        // Not invoking the callback:  the method should not be run
        testResult.status = ITestResult.SUCCESS
    }

    @Test
    fun verify() {
        Reporter.getCurrentTestResult().setAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE, "true")
    }
}
