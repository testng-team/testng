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

  @Test(description = "GITHUB-2830")
  public void toStringShouldBeFailsafe() {
    Unrenderable unrenderable = new Unrenderable();

    // Every caller of this is a report or a console line, so it answers what Object#toString() says
    // for a class that does not override it, rather than ending the report that asked.
    assertThat(Utils.toString(unrenderable)).isEqualTo(identityOf(unrenderable));
    assertThat(Utils.toString(unrenderable, Unrenderable.class))
        .isEqualTo(identityOf(unrenderable));
  }

  @Test(
      description = "An array is rendered by its contents, so one bad element loses the whole one")
  public void toStringOfAnArrayIsFailsafeToo() {
    Object[] array = {new Unrenderable()};

    assertThat(Utils.toString(array)).isEqualTo(identityOf(array));
  }

  @Test(description = "An Error is guarded too, which a RuntimeException-only catch would not be")
  public void toStringIsFailsafeAgainstAnError() {
    Overflowing overflowing = new Overflowing();

    assertThat(Utils.toString(overflowing)).isEqualTo(identityOf(overflowing));
  }

  /** What {@link Object#toString()} would have answered for a class that does not override it. */
  private static String identityOf(Object value) {
    return value.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(value));
  }

  /** A value that cannot be asked what it is, as opposed to one that answers {@code null}. */
  private static final class Unrenderable {
    @Override
    public String toString() {
      throw new IllegalStateException("this value cannot be rendered");
    }
  }

  /** Unrenderable like {@link Unrenderable}, but failing the way a recursive toString() does. */
  private static final class Overflowing {
    @Override
    public String toString() {
      throw new StackOverflowError();
    }
  }

  // exception which cannot be printed
  private static class ThrowingException extends RuntimeException {
    @Override
    public String getMessage() {
      throw new IllegalStateException("message not available");
    }
  }
}
