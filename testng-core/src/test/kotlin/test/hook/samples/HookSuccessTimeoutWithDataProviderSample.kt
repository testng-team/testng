package test.hook.samples

import test.hook.*

import java.util.UUID
import org.testng.IHookCallBack
import org.testng.IHookable
import org.testng.ITestResult
import org.testng.Reporter
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class HookSuccessTimeoutWithDataProviderSample : IHookable {

    override fun run(callBack: IHookCallBack, testResult: ITestResult) {
        testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true")
        testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.parameters)
        callBack.runTestMethod(testResult)
    }

    @DataProvider
    fun dp(): test.TestData = arrayOf(
        arrayOf(UUID.randomUUID())
    )

    @Test(dataProvider = "dp", timeOut = 100)
    fun verify(uuid: UUID) {
        Reporter.getCurrentTestResult().setAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE, uuid)
    }
}
