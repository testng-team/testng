package test.hook.samples

import test.hook.HOOK_INVOKED_ATTRIBUTE
import test.hook.HOOK_METHOD_INVOKED_ATTRIBUTE
import test.hook.HOOK_METHOD_PARAMS_ATTRIBUTE

import java.util.UUID
import org.testng.IHookCallBack
import org.testng.IHookable
import org.testng.ITestResult
import org.testng.Reporter
import org.testng.annotations.Test

class HookSuccessTimeoutSample : IHookable {

    override fun run(callBack: IHookCallBack, testResult: ITestResult) {
        testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
        testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.parameters)
        callBack.runTestMethod(testResult)
    }

    @Test(timeOut = 100)
    fun verify() {
        Reporter.getCurrentTestResult()
            .setAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE, UUID.randomUUID())
    }
}
