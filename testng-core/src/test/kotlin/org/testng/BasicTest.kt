package org.testng

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test
import test.SimpleBaseTest

class BasicTest : SimpleBaseTest() {

    @Test
    fun `basic test as sample`() {
        val listener = run(BasicSample::class.java)
        assertThat(listener.failedMethodNames).containsExactly("test")
        assertThat(listener.succeedMethodNames).isEmpty()
        assertThat(listener.skippedMethodNames).isEmpty()
    }
}
