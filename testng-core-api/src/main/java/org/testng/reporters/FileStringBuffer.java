package org.testng.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.jspecify.annotations.Nullable;
import org.testng.log4testng.Logger;

/**
 * A string buffer that flushes its content to a temporary file whenever the internal string buffer
 * becomes larger than MAX. If the buffer never reaches that size, no file is ever created and
 * everything happens in memory, so the overhead compared to StringBuffer/StringBuilder is minimal.
 *
 * <p>Note: calling toString() will force the entire string to be loaded in memory, use toWriter()
 * if you need to avoid this.
 *
 * <p>This class is not multi thread safe.
 *
 * @since Nov 9, 2012
 */
public class FileStringBuffer implements IBuffer {
  private static final int MAX = 100000;
  private static final boolean VERBOSE = RuntimeBehavior.verboseMode();
  private static final Logger LOGGER = Logger.getLogger(FileStringBuffer.class);

  private @Nullable File m_file;
  private StringBuilder m_sb = new StringBuilder();
  private final int m_maxCharacters;

  /**
   * What went wrong while spilling, if anything did.
   *
   * <p>Recorded rather than raised on the spot. Appending happens deep inside building a document
   * -- for the JUnit report, inside a listener, which TestNG calls with nothing around it -- so
   * raising there ends the run at an arbitrary tag. Raising when the buffer is asked for its
   * content instead puts the failure where the report is produced, and names the report rather than
   * the tag that happened to overflow.
   */
  private @Nullable IOException spillFailure;

  public FileStringBuffer() {
    this(MAX);
  }

  public FileStringBuffer(int maxCharacters) {
    m_maxCharacters = maxCharacters;
  }

  @Override
  public FileStringBuffer append(CharSequence s) {
    if (s == null) {
      throw new IllegalArgumentException(
          "CharSequence (Argument 0 of FileStringBuffer#append) should not be null");
    }
    //    m_sb.append(s);
    if (m_sb.length() > m_maxCharacters) {
      flushToFile();
    }
    if (s.length() < MAX) {
      // Small string, add it to our internal buffer
      m_sb.append(s);
    } else {
      // Big string, add it to the temporary file directly
      flushToFile();
      if (spillFailure != null) {
        // Same rule as flushToFile: nothing more goes to a file that is already damaged.
        return this;
      }
      try {
        File file = temporaryFile();
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, true /* append */)) {
          copy(new StringReader(s.toString()), writer);
        }
      } catch (IOException e) {
        recordSpillFailure(e);
      }
    }
    return this;
  }

  @Override
  public void toWriter(Writer fw) {
    if (fw == null) {
      throw new IllegalArgumentException(
          "Writer (Argument 0 of FileStringBuffer#toWriter) should not be null");
    }
    // The flush first, then the guard: the fault that lands inside this very flush -- the disk
    // filling between the last append and the report, a cleaner removing the file -- is recorded
    // by it, and a guard that ran before it would let the read go on and hand back the file
    // without the builder's tail.
    if (m_file != null) {
      flushToFile();
    }
    requireNothingWentWrongSpilling();
    try {
      BufferedWriter bw = new BufferedWriter(fw);
      if (m_file == null) {
        bw.write(m_sb.toString());
        bw.close();
      } else {
        try (FileReader reader = new FileReader(m_file, StandardCharsets.UTF_8)) {
          copy(reader, bw);
        }
        bw.flush();
      }
    } catch (IOException e) {
      // Not logged and swallowed: the caller would receive a document silently missing everything
      // this buffer held, which is what toString() has always refused to do. The wording covers
      // both branches above -- a buffer that never spilled has no temporary file, and the fault
      // there is the destination, which is the likelier one while index.html is being written.
      throw new IllegalStateException("A buffer could not be written out", e);
    }
  }

  private static void copy(Reader input, Writer output) throws IOException {
    char[] buf = new char[MAX];
    while (true) {
      int length = input.read(buf);
      if (length < 0) {
        break;
      }
      output.write(buf, 0, length);
    }
  }

  private void flushToFile() {
    if (m_sb.length() == 0) {
      return;
    }
    if (spillFailure != null) {
      // Already damaged. Retrying is not safe -- a failed write may have left part of the builder
      // on disk, so writing it again would duplicate it -- and nothing will read this buffer out
      // now, so what it still holds is discarded rather than grown without bound.
      m_sb = new StringBuilder();
      return;
    }

    try {
      File file = temporaryFile();
      p("Size " + m_sb.length() + ", flushing to " + file);
      try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8, true /* append */)) {
        fw.append(m_sb);
      }
    } catch (IOException e) {
      recordSpillFailure(e);
    }
    m_sb = new StringBuilder();
  }

  /** Keeps the first fault: the ones after it are consequences of a buffer already damaged. */
  private void recordSpillFailure(IOException e) {
    if (spillFailure == null) {
      spillFailure = e;
    }
  }

  /** Raises here, where the content is asked for, rather than where the spill went wrong. */
  private void requireNothingWentWrongSpilling() {
    if (spillFailure != null) {
      throw new IllegalStateException("A buffer could not be written out", spillFailure);
    }
  }

  /**
   * Creates the temporary file on first use.
   *
   * <p>Not left to {@link #flushToFile()}: that one answers early when the in-memory builder is
   * empty, which it is when the very first thing appended is a string larger than MAX -- and it
   * then went straight on to write to the file it had just decided not to create.
   *
   * @return the file this buffer spills to
   * @throws IOException if it cannot be created, which both callers record rather than raise
   */
  private File temporaryFile() throws IOException {
    if (m_file == null) {
      m_file = File.createTempFile("testng", "fileStringBuffer");
      m_file.deleteOnExit();
      p("Created temp file " + m_file);
    }
    return m_file;
  }

  private static void p(String s) {
    if (VERBOSE) {
      LOGGER.info("[FileStringBuffer] " + s);
    }
  }

  @Override
  public String toString() {
    // The flush first, then the guard, for the reason given in toWriter.
    if (m_file != null) {
      flushToFile();
    }
    requireNothingWentWrongSpilling();
    if (m_file == null) {
      return m_sb.toString();
    }
    try {
      return new String(Files.readAllBytes(m_file.toPath()), StandardCharsets.UTF_8);
    } catch (IOException e) {
      // Named rather than logged and answered as null: every caller dereferences the result
      // immediately, so the alternative is a NullPointerException one frame further away.
      throw new IllegalStateException("The temporary file of a buffer could not be read back", e);
    }
  }
}
