package test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringWriter;
import org.testng.annotations.Test;
import org.testng.reporters.FileStringBuffer;

/**
 * What a buffer gives back, whether or not it spilled to its temporary file.
 *
 * <p>Both readers are checked on every shape, because they read the file back differently -- {@code
 * toString} through {@code Files.readAllBytes}, {@code toWriter} through a {@code FileReader} --
 * and the reporters use one or the other depending on the caller.
 */
public class FileStringBufferTest {

  /**
   * The size above which an appended string bypasses the in-memory builder and is written to the
   * file directly. It is FileStringBuffer's own MAX, which is not the flush threshold the
   * constructor takes.
   */
  private static final int DIRECT_TO_FILE = 100_000;

  private static final String TEN = "0123456789";

  @Test
  public void aBufferThatNeverFilledUpGivesItsContentBack() {
    FileStringBuffer buffer = new FileStringBuffer(100);
    buffer.append(TEN);

    assertThat(buffer.toString()).isEqualTo(TEN);
    assertThat(written(buffer)).isEqualTo(TEN);
  }

  @Test
  public void aBufferThatFilledUpSeveralTimesGivesItsContentBack() {
    // A threshold of five characters, so each append but the first flushes what came before it.
    FileStringBuffer buffer = new FileStringBuffer(5);
    buffer.append(TEN);
    buffer.append(TEN);
    buffer.append(TEN);

    assertThat(buffer.toString()).isEqualTo(TEN + TEN + TEN);
    assertThat(written(buffer)).isEqualTo(TEN + TEN + TEN);
  }

  @Test
  public void aStringLargerThanTheDirectToFileSizeIsGivenBackWholeAfterASmallerOne() {
    String large = TEN.repeat(DIRECT_TO_FILE / TEN.length());
    FileStringBuffer buffer = new FileStringBuffer(5);
    buffer.append(TEN);
    buffer.append(large);

    assertThat(buffer.toString()).isEqualTo(TEN + large);
    assertThat(written(buffer)).isEqualTo(TEN + large);
  }

  @Test
  public void aStringLargerThanTheDirectToFileSizeIsGivenBackWholeWhenItComesFirst() {
    // The file is created as this buffer spills, and nothing had spilled yet: appending a string
    // this size first went straight to a file that had not been created, and threw a
    // NullPointerException out of a public method.
    String large = TEN.repeat(DIRECT_TO_FILE / TEN.length());
    FileStringBuffer buffer = new FileStringBuffer(5);
    buffer.append(large);

    assertThat(buffer.toString()).isEqualTo(large);
    assertThat(written(buffer)).isEqualTo(large);
  }

  @Test
  public void appendingNothingIsRejectedByName() {
    assertThatThrownBy(() -> new FileStringBuffer(5).append(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("should not be null");
  }

  private static String written(FileStringBuffer buffer) {
    StringWriter writer = new StringWriter();
    buffer.toWriter(writer);
    return writer.toString();
  }
}
