package test.hook.samples.issue2251

import java.lang.reflect.InvocationTargetException
import org.testng.IHookCallBack
import org.testng.IHookable
import org.testng.ITestResult

/**
 * This test class apes on a bare essential What Spring TestNG provides as a base class in terms of
 * running Spring based tests. The following methods have been duplicated from
 * org.springframework.test.context.testng.AbstractTestNGSpringContextTests to simulate the bug. 1.
 * throwAsUncheckedException() 2. getTestResultException() 3. throwAs()
 */
open class AbstractBaseSample : IHookable {

    override fun run(callBack: IHookCallBack, testResult: ITestResult) {
        callBack.runTestMethod(testResult)
        val t = getTestResultException(testResult)
        t?.let {
            throwAsUncheckedException(it)
        }
    }

    @SuppressWarnings("unchecked")
    private fun throwAs(t: Throwable) {
        throw  t
    }

    private fun throwAsUncheckedException(t: Throwable) {
        throwAs(t)
    }

    private fun getTestResultException(testResult: ITestResult): Throwable? {
        var testResultException = testResult.throwable
        if (testResultException is InvocationTargetException) {
            testResultException = testResultException.cause
        }
        return testResultException
    }
}
