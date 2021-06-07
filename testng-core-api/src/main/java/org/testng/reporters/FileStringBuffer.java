package org.testng.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
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
  private static int MAX = 100000;
  private static final boolean VERBOSE = RuntimeBehavior.verboseMode();
  private static final Logger LOGGER = Logger.getLogger(FileStringBuffer.class);

  private File m_file;
  private StringBuilder m_sb = new StringBuilder();
  private final int m_maxCharacters;

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
      try (FileWriter writer = new FileWriter(m_file, true /* append */)) {
        copy(new StringReader(s.toString()), writer);
      } catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
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
    try {
      BufferedWriter bw = new BufferedWriter(fw);
      if (m_file == null) {
        bw.write(m_sb.toString());
        bw.close();
      } else {
        flushToFile();
        try (FileReader reader = new FileReader(m_file)) {
          copy(reader, bw);
        }
        bw.flush();
      }
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  private static void copy(Reader input, Writer output) throws IOException {
    char[] buf = new char[MAX];
    while (true) {
      int length = input.read(buf);
      if (length < 0) break;
      output.write(buf, 0, length);
    }
  }

  private void flushToFile() {
    if (m_sb.length() == 0) return;

    if (m_file == null) {
      try {
        m_file = File.createTempFile("testng", "fileStringBuffer");
        m_file.deleteOnExit();
        p("Created temp file " + m_file);
      } catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

    p("Size " + m_sb.length() + ", flushing to " + m_file);
    try (FileWriter fw = new FileWriter(m_file, true /* append */)) {
      fw.append(m_sb);
    } catch (IOException e) {
      e.printStackTrace();
    }
    m_sb = new StringBuilder();
  }

  private static void p(String s) {
    if (VERBOSE) {
      LOGGER.info("[FileStringBuffer] " + s);
    }
  }

  @Override
  public String toString() {
    String result = null;
    if (m_file != null) {
      flushToFile();
      try {
        result = new String(Files.readAllBytes(m_file.toPath()));
      } catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
      }
    } else {
      result = m_sb.toString();
    }
    return result;
  }
}
