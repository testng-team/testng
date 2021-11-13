package org.testng.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;
import static org.testng.internal.Utils.join;

import java.util.List;
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
    assertEquals(
        Utils.escapeUnicode(String.valueOf(INVALID_CHAR)), String.valueOf(REPLACEMENT_CHAR));
  }

  @Test
  public void createEmptyStringWhenJoiningEmptyListWithJoin() {
    List<String> emptyList = emptyList();
    assertEquals("", join(emptyList, ","));
  }

  @Test
  public void joinTwoStringsWithJoinStrings() {
    List<String> twoStrings = asList("one", "two");
    assertEquals("one,two", Utils.join(twoStrings, ","));
  }

  @Test
  public void createEmptyStringWhenJoiningEmptyListWithJoinStrings() {
    List<String> emptyList = emptyList();
    assertEquals("", Utils.join(emptyList, ","));
  }
}
