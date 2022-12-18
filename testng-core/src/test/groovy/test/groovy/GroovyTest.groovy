package test.groovy

import org.testng.IMethodInstance
import org.testng.IMethodInterceptor
import org.testng.ITestContext
import org.testng.ITestNGListener
import org.testng.TestListenerAdapter
import org.testng.annotations.Test
import test.SimpleBaseTest
import test.groovy.issue2854.AssertionsTestSample

import static org.assertj.core.api.Assertions.assertThat

class GroovyTest extends SimpleBaseTest {

    @Test
    void easyGroovySampleShouldWork() {
        def tng = create(EasyJUnitGroovySample)
        tng.setJUnit(true)
        def adapter = new TestListenerAdapter()
        tng.addListener((ITestNGListener)adapter)
        tng.run()

        assert adapter.failedTests.isEmpty()
        assert adapter.skippedTests.isEmpty()
        assert adapter.passedTests.size() == 1
    }

    @Test
    void specialNameGroovySampleShouldWork() {
        def tng = create(SpecialNameJUnitGroovySample)
        tng.setJUnit(true)
        def adapter = new TestListenerAdapter()
        tng.addListener((ITestNGListener)adapter)
        tng.run()

        assert adapter.failedTests.isEmpty()
        assert adapter.skippedTests.isEmpty()
        assert adapter.passedTests.size() == 1
    }

    @Test(description = "GITHUB-2360")
    void groovyInternalMethodsAreSkipped() {
        def tng = create Issue2360Sample
        def testMethodNames = []
        IMethodInterceptor methodInterceptor = { List<IMethodInstance> methods, ITestContext context ->
            testMethodNames = methods.collect { it.method.methodName }
            methods
        }
        tng.methodInterceptor = methodInterceptor
        tng.run()

        assertThat testMethodNames containsExactly "test1", "test2"
    }

    @Test(description = "GITHUB-2854")
    void ensureAssertionsWork() {
        def testng = create AssertionsTestSample
        testng.run()
        assertThat testng.status isEqualTo(0)
    }
}
