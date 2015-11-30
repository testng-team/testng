package org.testng.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Random;

/**
 * A string buffer that flushes its content to a temporary file whenever the internal
 * string buffer becomes larger than MAX. If the buffer never reaches that size, no file
 * is ever created and everything happens in memory, so the overhead compared to
 * StringBuffer/StringBuilder is minimal.
 *
 * Note: calling toString() will force the entire string to be loaded in memory, use
 * toWriter() if you need to avoid this.
 *
 * This class is not multi thread safe.
 *
 * @author Cedric Beust <cedric@beust.com>
 *
 * @since Nov 9, 2012
 */
public class FileStringBuffer implements IBuffer {
  private static int MAX = 100000;
  private static final boolean VERBOSE = System.getProperty("fileStringBuffer") != null;

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
      throw new IllegalArgumentException("CharSequence (Argument 0 of FileStringBuffer#append) should not be null");
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
      try {
        copy(new StringReader(s.toString()), new FileWriter(m_file, true /* append */));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return this;
  }

  @Override
  public void toWriter(Writer fw) {
    if (fw == null) {
      throw new IllegalArgumentException("Writer (Argument 0 of FileStringBuffer#toWriter) should not be null");
    }
    try {
      BufferedWriter bw = new BufferedWriter(fw);
      if (m_file == null) {
        bw.write(m_sb.toString());
        bw.close();
      } else {
        flushToFile();
        copy(new FileReader(m_file), bw);
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  private static void copy(Reader input, Writer output)
      throws IOException {
    char[] buf = new char[MAX];
    while (true) {
      int length = input.read(buf);
      if (length < 0) break;
      output.write(buf, 0, length);
    }

    try {
      input.close();
    } catch (IOException ignore) {
    }
    try {
      output.close();
    } catch (IOException ignore) {
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
        e.printStackTrace();
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
      System.out.println("[FileStringBuffer] " + s);
    }
  }

  @Override
  public String toString() {
    String result = null;
    if (m_file != null) {
      flushToFile();
      try {
        result = Files.readFile(m_file);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      result = m_sb.toString();
    }
    return result;
  }

  private static void save(File expected, String s) throws IOException {
    expected.delete();
    try (FileWriter expectedWriter = new FileWriter(expected)) {
      expectedWriter.append(s);
    }
  }

  public static void main(String[] args) throws IOException {
    String s = "abcdefghijklmnopqrstuvwxyz";
    FileStringBuffer fsb = new FileStringBuffer(10);
    StringBuilder control = new StringBuilder();
    Random r = new Random();
    for (int i = 0; i < 1000; i++) {
      int start = Math.abs(r.nextInt() % 26);
      int length = Math.abs(r.nextInt() % (26 - start));
      String fragment = s.substring(start, start + length);
      p("... Appending " + fragment);
      fsb.append(fragment);
      control.append(fragment);
    }

    File expected = new File("/tmp/expected");
    expected.delete();
    FileWriter expectedWriter = new FileWriter(expected);
    expectedWriter.append(control);
    expectedWriter.close();

    File actual = new File("/tmp/actual");
    actual.delete();
    FileWriter actualWriter = new FileWriter(actual);
    fsb.toWriter(actualWriter);
    actualWriter.close();
//    Assert.assertEquals(fsb.toString(), control.toString());
  }

}
