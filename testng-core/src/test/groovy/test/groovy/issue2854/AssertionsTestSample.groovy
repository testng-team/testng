package test.groovy.issue2854

import org.testng.annotations.Test

import static org.assertj.core.api.Assertions.assertThat

class AssertionsTestSample {

    @Test
    void testAssertEqualsWorksWithBooleans() {
        assertThat(true).isEqualTo(true)
        assertThat(true).as("Sample Message").isEqualTo(true)
    }

    @Test
    void testAssertEqualsWorksWithBytes() {
        assertThat(Byte.valueOf((byte) 10)).isEqualTo(Byte.valueOf((byte) 10))
        assertThat(Byte.valueOf((byte) 10)).as("Sample Message").isEqualTo(Byte.valueOf((byte) 10))
    }

    @Test
    void testAssertEqualsWorksWithChars() {
        assertThat(Character.valueOf((char) 10)).isEqualTo(Character.valueOf((char) 10))
        assertThat(Character.valueOf((char) 10)).as("Sample Message").isEqualTo(Character.valueOf((char) 10))
    }

    @Test
    void testAssertEqualsWorksWithShorts() {
        assertThat(Short.valueOf((short) 10)).isEqualTo(Short.valueOf((short) 10))
        assertThat(Short.valueOf((short) 10)).as("Sample Message").isEqualTo(Short.valueOf((short) 10))
    }

    @Test
    void testAssertEqualsWorksWithInts() {
        assertThat(10).isEqualTo(10)
        assertThat(10).as("Sample Message").isEqualTo(10)
    }

    @Test
    void testAssertEqualsWorksWithLongs() {
        assertThat(Long.valueOf((long) 10)).isEqualTo(Long.valueOf((long) 10))
        assertThat(Long.valueOf((long) 10)).as("Sample Message").isEqualTo(Long.valueOf((long) 10))
    }

    @Test
    void testAssertEqualsWorksWithFloats() {
        assertThat(Float.valueOf((float) 10)).isEqualTo(Float.valueOf((float) 10))
        assertThat(Float.valueOf((float) 10)).as("Sample Message").isEqualTo(Float.valueOf((float) 10))
    }

    @Test
    void testAssertEqualsWorksWithDoubles() {
        assertThat(Double.valueOf((double) 10)).isEqualTo(Double.valueOf((double) 10))
        assertThat(Double.valueOf((double) 10)).as("Sample Message").isEqualTo(Double.valueOf((double) 10))
    }

}
