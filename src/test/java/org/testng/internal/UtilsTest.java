package org.testng.internal;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link Utils}.
 *
 * @author Tomas Pollak
 */
public class UtilsTest {
	private static final char INVALID_CHAR = 0xFFFE;
	private static final char REPLACEMENT_CHAR = 0xFFFD;

	@Test
	public void escapeUnicode() {
		assertEquals(Utils.escapeUnicode("test"), "test");
		assertEquals(Utils.escapeUnicode(String.valueOf(INVALID_CHAR)),
				String.valueOf(REPLACEMENT_CHAR));

	}
}
