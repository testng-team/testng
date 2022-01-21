package test.attributes.samples.issue2346

import org.testng.ITestListener
import org.testng.ITestResult

val data = mutableMapOf<String, Boolean>()

class LocalTestListener : ITestListener {

    override fun onTestStart(result: ITestResult) {
        val key = "onTestStart_" + result.method.qualifiedName
        data.putIfAbsent(key, result.attributeNames.isEmpty())
    }

    override fun onTestSuccess(iTestResult: ITestResult) {
        val key = "onTestSuccess_" + iTestResult.method.qualifiedName
        data.putIfAbsent(key, iTestResult.attributeNames.isEmpty())
    }

    override fun onTestFailure(iTestResult: ITestResult) {
        val key = "onTestFailure_" + iTestResult.method.qualifiedName
        data.putIfAbsent(key, iTestResult.attributeNames.isEmpty())
    }

    override fun onTestSkipped(iTestResult: ITestResult) {
        val key = "onTestSkipped_" + iTestResult.method.qualifiedName
        data.putIfAbsent(key, iTestResult.attributeNames.isEmpty())
    }
}
