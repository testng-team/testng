package org.testng.util

import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

class StringsTest {
    @Test
    fun testValueOf() {
        val input = mapOf(
            Pair("skill", "Kung-Fu"),
            Pair("expertise-level", "Master"),
            Pair("name", "Po")
        )
        val expected = "Kung-Fu Master Po"
        val actual = Strings.valueOf(input)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testEscapeHtml() {
        val input = "&<>"
        val expected = "&amp;&lt;&gt;"
        val actual = Strings.escapeHtml(input)
        assertThat(actual).isEqualTo(expected)
    }
}
