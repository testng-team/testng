package test.hook.samples.issue2266

import org.testng.Assert
import org.testng.IHookCallBack
import org.testng.IHookable
import org.testng.ITestResult
import org.testng.annotations.Test

class TestClassSample : IHookable {

    private var counter = 1

    override fun run(callBack: IHookCallBack, testResult: ITestResult?) {
        callBack.runTestMethod(testResult)
        takeIf { testResult?.throwable != null }.apply {
            for (i in 1..3) {
                callBack.runTestMethod(testResult)
                if (testResult?.throwable == null) {
                    break
                }
            }
        }
    }

    @Test(description = "GITHUB-2266")
    fun runTest(){
        if (counter++ != 2) {
            Assert.fail()
        }
    }
}
