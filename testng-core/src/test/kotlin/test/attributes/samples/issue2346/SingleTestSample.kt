package test.attributes.samples.issue2346

import org.testng.Assert.fail

import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class SingleTestSample : BaseTestSample() {
    @BeforeMethod
    fun start() {
        fail()
    }

    @Test
    fun test() {
    }
}
