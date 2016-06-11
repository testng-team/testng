package test.groovy

import org.testng.ITestNGListener
import org.testng.TestListenerAdapter
import org.testng.annotations.Test
import test.SimpleBaseTest

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

    @Test
    void spockSampleShouldWork() {
        def tng = create(SpockSample)
        tng.setJUnit(true)
        def adapter = new TestListenerAdapter()
        tng.addListener((ITestNGListener)adapter)
        tng.run()

        assert adapter.failedTests.isEmpty()
        assert adapter.skippedTests.isEmpty()
        assert adapter.passedTests.size() == 1
    }
}
