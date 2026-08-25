package org.testng.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.internal.Utils.join;

import java.util.List;
import org.testng.annotations.DataProvider;
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
    assertThat(Utils.escapeUnicode("test")).isEqualTo("test");
    assertThat(Utils.escapeUnicode(String.valueOf(INVALID_CHAR)))
        .isEqualTo(String.valueOf(REPLACEMENT_CHAR));
  }

  @Test
  public void createEmptyStringWhenJoiningEmptyListWithJoin() {
    List<String> emptyList = emptyList();
    assertThat("").isEqualTo(join(emptyList, ","));
  }

  @Test
  public void joinTwoStringsWithJoinStrings() {
    List<String> twoStrings = asList("one", "two");
    assertThat("one,two").isEqualTo(join(twoStrings, ","));
  }

  @Test
  public void createEmptyStringWhenJoiningEmptyListWithJoinStrings() {
    List<String> emptyList = emptyList();
    assertThat("").isEqualTo(join(emptyList, ","));
  }

  /**
   * splitOnLiteral has to answer what String.split answers for a separator that happens to contain
   * no regular expression syntax, so that swapping one for the other changes nothing but the
   * reading of the separator. Each case below is asserted against String.split as well as against
   * the expected value, so the two cannot drift apart silently.
   */
  @Test(dataProvider = "literalSplits")
  public void splitOnLiteralMatchesStringSplit(String value, String separator, String[] expected) {
    assertThat(Utils.splitOnLiteral(value, separator)).containsExactly(expected);
    assertThat(Utils.splitOnLiteral(value, separator)).containsExactly(value.split(separator));
  }

  @DataProvider(name = "literalSplits")
  public Object[][] literalSplits() {
    return new Object[][] {
      {"a,b", ",", new String[] {"a", "b"}},
      {"a,b,", ",", new String[] {"a", "b"}},
      {"a,b,,,", ",", new String[] {"a", "b"}},
      {",a", ",", new String[] {"", "a"}},
      {"a", ",", new String[] {"a"}},
      {"", ",", new String[] {""}},
      {",", ",", new String[0]},
      {"a]]>b", "]]>", new String[] {"a", "b"}},
      {"1 2  3", " ", new String[] {"1", "2", "", "3"}},
    };
  }

  /**
   * The separator is literal, which is the whole point: String.split would read it as a pattern.
   */
  @Test
  public void splitOnLiteralDoesNotReadTheSeparatorAsAPattern() {
    assertThat(Utils.splitOnLiteral("a.b", ".")).containsExactly("a", "b");
    assertThat("a.b".split(".")).isEmpty();
  }

  @Test
  public void splitCommaSeparatedTrimsEveryPieceAndDropsTheEmptyOnes() {
    assertThat(Utils.splitCommaSeparated("a.B, a.C")).containsExactly("a.B", "a.C");
    assertThat(Utils.splitCommaSeparated(" a.B ,,, a.C ,")).containsExactly("a.B", "a.C");
    assertThat(Utils.splitCommaSeparated("a.B")).containsExactly("a.B");
    assertThat(Utils.splitCommaSeparated("")).isEmpty();
    assertThat(Utils.splitCommaSeparated(" , ,")).isEmpty();
  }

  @Test
  public void buildStackTraceShouldBeFailsafe() {
    // e.g. mocks of Exception classes may throw exception on exception.toString()
    RuntimeException ex = new ThrowingException();
    String stackTrace = Utils.longStackTrace(ex, true);

    assertThat(stackTrace)
        .contains("org.testng.internal.UtilsTest$ThrowingException")
        .contains("java.lang.IllegalStateException: message not available");
  }

  // exception which cannot be printed
  private static class ThrowingException extends RuntimeException {
    @Override
    public String getMessage() {
      throw new IllegalStateException("message not available");
    }
  }
}
