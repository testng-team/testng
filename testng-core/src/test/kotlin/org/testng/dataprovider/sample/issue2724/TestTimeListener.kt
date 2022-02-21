package org.testng.dataprovider.sample.issue2724

import org.testng.ITestContext
import org.testng.ITestListener

class TestTimeListener : ITestListener {
    private var startTime: Long = 0

    override fun onStart(context: ITestContext?) {
        startTime = System.currentTimeMillis()
    }

    override fun onFinish(context: ITestContext?) {
        testRunTime = System.currentTimeMillis() - startTime
    }

    companion object {
        var testRunTime: Long = 0
    }
}
