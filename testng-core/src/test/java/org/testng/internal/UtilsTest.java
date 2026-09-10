package org.testng.internal;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testng.internal.Utils.join;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.XMLStringBuffer;

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

  @Test(
      dataProvider = "failuresOnTheWayOut",
      description = "A report that failed part way leaves no file, not a plausible one")
  public void aBufferThatGivesUpPartWayLeavesNoFileBehind(Throwable failure) throws Exception {
    // The prefix is written before the buffer, so a buffer that raises leaves a page header and no
    // body -- 40 bytes that open as a report and say nothing about why they are empty.
    XMLStringBuffer panel = new XMLStringBuffer("");
    panel.addString("<body>never written</body>");
    File directory = Files.createTempDirectory("utils-writeutf8").toFile();
    directory.deleteOnExit();

    assertThatThrownBy(
            () ->
                Utils.writeUtf8File(
                    directory.getAbsolutePath(),
                    "index.html",
                    new FailingBuffer(panel, failure),
                    "<html><head><title>report</title></head>"))
        .isSameAs(failure);

    assertThat(new File(directory, "index.html")).doesNotExist();
  }

  /**
   * An {@code Error} as well as a {@code RuntimeException}, because the failure this guard exists
   * for is the {@code OutOfMemoryError} of GITHUB-1259 and GITHUB-2334, and {@code Error} is a
   * sibling of {@code RuntimeException} rather than a subtype. It is also the case where the stub
   * matters most: nothing downstream is going to write a better file over it.
   */
  @DataProvider(name = "failuresOnTheWayOut")
  public Object[][] failuresOnTheWayOut() {
    return new Object[][] {
      {new IllegalStateException("A buffer could not be written out")},
      {new OutOfMemoryError("Java heap space")},
    };
  }

  @Test(description = "The file is removed when the writer itself fails, not only the buffer")
  public void aFileTheWriterCouldNotBeOpenedOnIsNotLeftBehind() throws Exception {
    // The failure this stands in for is the disk filling up, which surfaces as an IOException from
    // the final flush of the try-with-resources and leaves a nearly complete report rather than an
    // obvious stub. That one cannot be provoked on every platform the build runs on; occupying the
    // report's own path reaches the same catch by the one means that behaves identically
    // everywhere, and asserts the same thing -- a report that could not be written leaves nothing.
    File directory = Files.createTempDirectory("utils-writeutf8-io").toFile();
    directory.deleteOnExit();
    File occupied = new File(directory, "index.html");
    assertThat(occupied.mkdir()).isTrue();

    XMLStringBuffer panel = new XMLStringBuffer("");
    panel.addString("<body>never written</body>");

    // An IOException is reported, not raised: writeUtf8File has never thrown one at its callers.
    Utils.writeUtf8File(directory.getAbsolutePath(), "index.html", panel, null);

    assertThat(occupied).doesNotExist();
  }

  /** Raises where a buffer whose temporary file has gone raises, which is on the way out. */
  private static final class FailingBuffer extends XMLStringBuffer {
    private final Throwable failure;

    FailingBuffer(XMLStringBuffer content, Throwable failure) {
      super(content.getStringBuffer(), "");
      this.failure = failure;
    }

    @Override
    public void toWriter(Writer fw) {
      if (failure instanceof Error) {
        throw (Error) failure;
      }
      throw (RuntimeException) failure;
    }
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
