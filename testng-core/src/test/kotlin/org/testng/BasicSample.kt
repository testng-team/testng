package org.testng

import org.testng.Assert.fail
import org.testng.annotations.Test

class BasicSample {

    @Test
    fun test() {
        fail("BOUM")
    }
}
