package test.hook.samples.issue2257

import org.testng.Assert
import org.testng.IConfigurable
import org.testng.IConfigureCallBack
import org.testng.ITestResult
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeSuite
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class TestClassSample : IConfigurable {

    private var counter = 1

    override fun run(callBack: IConfigureCallBack, testResult: ITestResult?) {
        callBack.runConfigurationMethod(testResult)
        takeIf { testResult?.throwable != null }.apply {
            for (i in 1..3) {
                callBack.runConfigurationMethod(testResult)
                if (testResult?.throwable == null) {
                    break
                }
            }
        }
    }

    @BeforeSuite
    fun beforeSuite() {
        runConfiguration()
    }

    @BeforeTest
    fun beforeTest() {
        runConfiguration()
    }

    @BeforeClass
    fun beforeClass() {
        runConfiguration()
    }

    @BeforeMethod
    fun beforeMethod() {
        runConfiguration()
    }

    @Test
    fun runTest() {
    }

    private fun runConfiguration() {
        if (counter++ == 2) {
            counter = 1
        } else {
            Assert.fail()
        }
    }
}
