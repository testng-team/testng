package test.hook.samples

import javax.inject.Named
import org.testng.Assert
import org.testng.IHookCallBack
import org.testng.IHookable
import org.testng.ITestResult
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import test.TestData

class HookSuccessDynamicParametersSample : IHookable {

    override fun run(callBack: IHookCallBack, testResult: ITestResult) {
        val method = testResult.method.constructorOrMethod.method
        for (i in 0 until callBack.parameters.size) {
            method.parameterAnnotations[i].forEach { annotation ->
                annotation.takeIf { it is Named }.run {
                    val named = this as Named
                    callBack.parameters[0] = callBack.parameters[0].toString() + named.value
                }
            }
            callBack.runTestMethod(testResult)
        }
    }

    @DataProvider
    fun dp(): TestData = arrayOf(
        arrayOf("foo", "test")
    )

    @Test(dataProvider = "dp")
    fun verify(@Named("bar") bar: String, test: String) {
        Assert.assertEquals(bar, "foobar")
        Assert.assertEquals(test, "test")
    }
}
