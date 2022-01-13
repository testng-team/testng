package org.testng.util

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

class StringsTest {
    @Test
    fun joinEmptyArray() {
        val emptyArray = arrayOf<String>()
        assertThat(Strings.join(",", emptyArray)).isEmpty()
    }

    @Test
    fun joinArrayWithOneElement() {
        val array = arrayOf("one")
        assertThat(Strings.join(",", array)).isEqualTo("one")
    }

    @Test
    fun joinArrayWithTwoElements() {
        val array = arrayOf("one", "two")
        assertThat(Strings.join(",", array)).isEqualTo("one,two")
    }
}
