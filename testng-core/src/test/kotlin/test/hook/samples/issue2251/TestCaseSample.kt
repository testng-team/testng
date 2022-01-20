package test.hook.samples.issue2251

import org.testng.annotations.Test

class TestCaseSample : AbstractBaseSample() {

    class NullExObj {
        override fun toString() = throw NullPointerException("expected")
    }

    @Test(timeOut = 1000000) // removing timeout fixes error output
    fun testError() {
        NullExObj().toString()
    }
}
