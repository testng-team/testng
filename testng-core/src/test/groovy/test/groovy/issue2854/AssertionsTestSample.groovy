package test.groovy.issue2854

import org.testng.annotations.Test

import static org.testng.Assert.assertEquals

class AssertionsTestSample {

    @Test
    void testAssertEqualsWorksWithBooleans() {
        assertEquals(true, true)
        assertEquals(true, true, "Sample Message")
    }

    @Test
    void testAssertEqualsWorksWithBytes() {
        assertEquals(Byte.valueOf((byte) 10), Byte.valueOf((byte) 10))
        assertEquals(Byte.valueOf((byte) 10), Byte.valueOf((byte) 10), "Sample Message")
    }

    @Test
    void testAssertEqualsWorksWithChars() {
        assertEquals(Character.valueOf((char) 10), Character.valueOf((char) 10))
        assertEquals(Character.valueOf((char) 10), Character.valueOf((char) 10), "Sample Message")
    }

    @Test
    void testAssertEqualsWorksWithShorts() {
        assertEquals(Short.valueOf((short) 10), Short.valueOf((short) 10))
        assertEquals(Short.valueOf((short) 10), Short.valueOf((short) 10), "Sample Message")
    }

    @Test
    void testAssertEqualsWorksWithInts() {
        assertEquals(10, 10)
        assertEquals(10, 10, "Sample Message")
    }

    @Test
    void testAssertEqualsWorksWithLongs() {
        assertEquals(Long.valueOf((long) 10), Long.valueOf((long) 10))
        assertEquals(Long.valueOf((long) 10), Long.valueOf((long) 10), "Sample Message")
    }

    @Test
    void testAssertEqualsWorksWithFloats() {
        assertEquals(Float.valueOf((float) 10), Float.valueOf((float) 10))
        assertEquals(Float.valueOf((float) 10), Float.valueOf((float) 10), "Sample Message")
    }

    @Test
    void testAssertEqualsWorksWithDoubles() {
        assertEquals(Double.valueOf((double) 10), Double.valueOf((double) 10))
        assertEquals(Double.valueOf((double) 10), Double.valueOf((double) 10), "Sample Message")
    }

}
