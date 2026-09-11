package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
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
            // Both spellings: stdout.encoding only exists from JDK 19, and before it System.out
            // follows file.encoding -- which this test sets to a charset that cannot hold what the
            // child prints back, so the assertion would fail on the way home rather than in the
            // spill file it is about.
            "-Dstdout.encoding=UTF-8",
            "-Dsun.stdout.encoding=UTF-8",
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

  @Test(
      description =
          "A spill that fails is raised when the content is asked for, not while appending")
  public void aFailedSpillIsRaisedOnTheWayOutRatherThanOnTheWayIn() throws Exception {
    // Appending happens deep inside building a document -- for the JUnit report, inside a listener
    // TestNG calls bare -- so raising there ends the run at whatever tag happened to overflow.
    // The buffer records the fault and refuses to hand anything over instead.
    FileStringBuffer buffer = spilledOnceWithATailHeldBack();
    File temporary = temporaryFileOf(buffer);
    try {
      buffer.append("CCCCCC");
      buffer.append("DDDDDD");
    } finally {
      assertThat(temporary.setWritable(true)).isTrue();
    }

    assertThatThrownBy(buffer::toString)
        .as("toString handed back a document with a hole in it")
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("could not be written out");
    assertThatThrownBy(() -> buffer.toWriter(new StringWriter()))
        .as("toWriter wrote out a document with a hole in it")
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("could not be written out");
  }

  @Test(
      description =
          "A spill that fails inside the reader's own flush is raised, not handed back short")
  public void aFaultInsideTheReadersOwnFlushIsRaisedRatherThanTruncating() throws Exception {
    // The guard used to run before the flush, so a fault landing in that flush -- the disk filling
    // between the last append and the report, a cleaner removing the file -- was recorded and the
    // read went on regardless, returning the file without the builder's tail. Only the second read
    // complained, by which time the report was written. Two buffers because the first read of
    // either records the fault, and the second reader would then be raising for the recorded one.
    FileStringBuffer forToString = spilledOnceWithATailHeldBack();
    File first = temporaryFileOf(forToString);
    FileStringBuffer forToWriter = spilledOnceWithATailHeldBack();
    File second = temporaryFileOf(forToWriter);
    try {
      assertThatThrownBy(forToString::toString)
          .as("toString handed back the file without the tail the builder still held")
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("could not be written out");
      assertThatThrownBy(() -> forToWriter.toWriter(new StringWriter()))
          .as("toWriter wrote out the file without the tail the builder still held")
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("could not be written out");
    } finally {
      assertThat(first.setWritable(true)).isTrue();
      assertThat(second.setWritable(true)).isTrue();
    }
  }

  /**
   * Spilled exactly once, with the next append still in the builder and the spill file made
   * read-only -- so the very first fault is the one the reader's own flush runs into. The caller
   * makes the file writable again.
   */
  private static FileStringBuffer spilledOnceWithATailHeldBack() throws Exception {
    FileStringBuffer buffer = new FileStringBuffer(4);
    buffer.append("AAAAAA");
    buffer.append("BBBBBB");
    assertThat(temporaryFileOf(buffer).setReadOnly()).isTrue();
    return buffer;
  }

  @Test(description = "A string large enough to go to the file directly stops at a recorded fault")
  public void aLargeStringAppendedAfterAFailedSpillDoesNotReachTheDisk() throws Exception {
    // flushToFile stops writing once a spill has failed, on the grounds that a failed write may
    // have left part of the builder on disk. The direct-to-file branch of append went on writing,
    // so anything of that size kept reaching the disk after the fault -- and the two branches
    // disagreed about whether that was safe.
    FileStringBuffer buffer = spilledOnceWithATailHeldBack();
    File temporary = temporaryFileOf(buffer);
    long spilled = temporary.length();
    try {
      buffer.append("CCCCCC");
    } finally {
      assertThat(temporary.setWritable(true)).isTrue();
    }

    // Writable again, so the write below would succeed if it were attempted.
    buffer.append(TEN.repeat(DIRECT_TO_FILE / TEN.length()));

    assertThat(temporary.length())
        .as("the spill file grew after the fault was recorded")
        .isEqualTo(spilled);
  }

  @Test(description = "Appending to a buffer whose spill failed does not raise")
  public void appendingAfterAFailedSpillIsSilent() throws Exception {
    // The half of the contract the report depends on: every push and pop after the fault has to
    // return normally, or the listener building the document dies on one of them.
    FileStringBuffer buffer = spilledOnceWithATailHeldBack();
    File temporary = temporaryFileOf(buffer);
    try {
      for (int i = 0; i < 50; i++) {
        buffer.append("CCCCCC");
      }
    } finally {
      assertThat(temporary.setWritable(true)).isTrue();
    }
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
        .hasMessageContaining("could not be written out");
  }

  /** The spill file, which no accessor exposes. */
  private static File temporaryFileOf(FileStringBuffer buffer) throws Exception {
    Field field = FileStringBuffer.class.getDeclaredField("m_file");
    field.setAccessible(true);
    return (File) Objects.requireNonNull(field.get(buffer), "the buffer has not spilled");
  }

  @Test(description = "The failure names what failed, on a buffer that has no temporary file")
  public void aBufferThatNeverSpilledBlamesTheDestination() {
    // One catch covers both branches of toWriter, and it used to say the temporary file could not
    // be read back -- on a buffer that has no temporary file, when it was the destination that
    // broke. A full disk while index.html is being written is the likelier of the two.
    FileStringBuffer buffer = new FileStringBuffer();
    buffer.append("short, so nothing ever spilled");

    assertThatThrownBy(() -> buffer.toWriter(new BrokenWriter()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageNotContaining("temporary file")
        .hasRootCauseMessage("the destination is broken");
  }

  /** A destination that fails the way a full disk does. */
  private static final class BrokenWriter extends Writer {
    @Override
    public void write(char[] characters, int offset, int length) throws IOException {
      throw new IOException("the destination is broken");
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
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
