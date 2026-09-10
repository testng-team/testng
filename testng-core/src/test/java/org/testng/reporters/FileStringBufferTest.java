package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

/**
 * What a buffer gives back, whether or not it spilled to its temporary file.
 *
 * <p>Both readers are checked on every shape, because they read the file back differently -- {@code
 * toString} through {@code Files.readAllBytes}, {@code toWriter} through a {@code FileReader} --
 * and the reporters use one or the other depending on the caller.
 */
public class FileStringBufferTest {

  /**
   * The size at which an appended string bypasses the in-memory builder and is written to the file
   * directly -- the condition is {@code s.length() < MAX}, so this length already goes to the file
   * and one character less does not. It is FileStringBuffer's own MAX, which is not the flush
   * threshold the constructor takes.
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
  public void aStringAtTheDirectToFileSizeIsGivenBackWholeAfterASmallerOne() {
    String large = TEN.repeat(DIRECT_TO_FILE / TEN.length());
    FileStringBuffer buffer = new FileStringBuffer(5);
    buffer.append(TEN);
    buffer.append(large);

    assertThat(buffer.toString()).isEqualTo(TEN + large);
    assertThat(written(buffer)).isEqualTo(TEN + large);
  }

  @Test
  public void aStringAtTheDirectToFileSizeIsGivenBackWholeWhenItComesFirst() {
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
  public void aStringJustBelowTheDirectToFileSizeIsGivenBackWhole() {
    // One character less takes the other branch of `s.length() < MAX`, which nothing exercised.
    String large = TEN.repeat(DIRECT_TO_FILE / TEN.length()).substring(1);
    FileStringBuffer buffer = new FileStringBuffer(5);
    buffer.append(TEN);
    buffer.append(large);

    assertThat(buffer.toString()).isEqualTo(TEN + large);
    assertThat(written(buffer)).isEqualTo(TEN + large);
  }

  @Test(description = "The spill file is UTF-8, whatever the platform default charset is")
  public void aSpilledBufferKeepsCharactersTheDefaultCharsetCannotHold() throws Exception {
    // The temporary file used to be written and read through the platform default charset, so a
    // character that charset cannot encode was lost at spill time -- before the page writer, which
    // is explicitly UTF-8, ever saw it. A child JVM because the default charset is fixed at start.
    String out = runProbeUnder("windows-1252");

    assertThat(out).isEqualTo(SUPPLEMENTARY);
  }

  /** U+1F600 and a combining acute: neither survives windows-1252. */
  private static final String SUPPLEMENTARY = new String(Character.toChars(0x1F600)) + "\u00e9";

  private static String runProbeUnder(String encoding) throws Exception {
    List<String> command =
        Arrays.asList(
            System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
            "-Dfile.encoding=" + encoding,
            "-Dstdout.encoding=UTF-8",
            "-cp",
            System.getProperty("java.class.path"),
            SpillProbe.class.getName());
    Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
    String output;
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      output = reader.lines().filter(line -> line.startsWith("SPILLED=")).findFirst().orElse("");
    }
    process.waitFor(2, TimeUnit.MINUTES);
    return output.replaceFirst("^SPILLED=", "");
  }

  /** Spills a buffer past its threshold and prints back the one part that has to survive. */
  public static final class SpillProbe {
    public static void main(String[] args) {
      FileStringBuffer buffer = new FileStringBuffer(5);
      buffer.append(SUPPLEMENTARY);
      buffer.append(TEN.repeat(DIRECT_TO_FILE / TEN.length()));
      String back = buffer.toString();
      System.out.println("SPILLED=" + back.substring(0, SUPPLEMENTARY.length()));
    }
  }

  @Test(description = "A flush that fails keeps what it could not write")
  public void aFailedFlushDoesNotDropWhatTheBufferHeld() throws Exception {
    // The builder used to be replaced whether or not the write had worked, so the characters it
    // still held vanished from the middle of the document and nothing was raised.
    FileStringBuffer buffer = new FileStringBuffer(4);
    buffer.append("AAAAAA");
    buffer.append("BBBBBB");
    File temporary = temporaryFileOf(buffer);
    assertThat(temporary.setReadOnly()).isTrue();
    try {
      assertThatThrownBy(() -> buffer.append("CCCCCC"))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("flush");
    } finally {
      assertThat(temporary.setWritable(true)).isTrue();
    }

    assertThat(buffer.toString()).isEqualTo("AAAAAABBBBBB");
  }

  @Test(description = "A buffer whose file is gone says so instead of answering an empty document")
  public void aBufferThatCannotBeReadBackSaysSo() throws Exception {
    // toWriter logged and returned, so a spilled panel whose temporary file disappeared was
    // dropped from the report in silence -- where toString() had always raised.
    FileStringBuffer buffer = new FileStringBuffer(5);
    buffer.append(TEN.repeat(DIRECT_TO_FILE / TEN.length()));
    assertThat(temporaryFileOf(buffer).delete()).isTrue();

    assertThatThrownBy(() -> buffer.toWriter(new StringWriter()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("read back");
  }

  /** The spill file, which no accessor exposes: these two tests are about what happens to it. */
  private static File temporaryFileOf(FileStringBuffer buffer) throws Exception {
    Field field = FileStringBuffer.class.getDeclaredField("m_file");
    field.setAccessible(true);
    return (File) Objects.requireNonNull(field.get(buffer), "the buffer has not spilled");
  }

  // The package this test now sits in is @NullMarked, and passing null is the point of the test.
  @SuppressWarnings("NullAway")
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
