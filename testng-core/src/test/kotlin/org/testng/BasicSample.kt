package org.testng

import org.assertj.core.api.Assertions.fail
import org.testng.annotations.Test

class BasicSample {

    @Test
    fun test() {
        fail<Nothing>("BOUM")
    }
}
