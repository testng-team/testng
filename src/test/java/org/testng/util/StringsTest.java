package org.testng.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link Strings}
 *
 * @author Spencer Gilson
 */
public class StringsTest {
    @Test
    public void joinEmptyArray() {
        String[] emptyArray = new String[0];
        assertEquals(Strings.join(",", emptyArray), "");
    }

    @Test
    public void joinArrayWithOneElement() {
        String[] array = new String[]{"one"};
        assertEquals(Strings.join(",", array), "one");
    }

    @Test
    public void joinArrayWithTwoElements() {
        String[] array = new String[]{"one", "two"};
        assertEquals(Strings.join(",", array), "one,two");
    }
}
