package org.testng.reporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.testng.Assert;

public class FileStringBuffer {
  private static int MAX = 10000;

  private File m_file;
  private StringBuilder m_sb = new StringBuilder();
  private final int m_maxCharacters;

  public FileStringBuffer() {
    this(MAX);
  }

  public FileStringBuffer(int maxCharacters) {
    m_maxCharacters = maxCharacters;

    try {
      m_file = File.createTempFile("testng", "fileStringBuffer");
      m_file.deleteOnExit();
      p("Created temp file " + m_file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void append(String s) {
    if (m_sb.length() > m_maxCharacters) {
      flushToFile();
    }
    m_sb.append(s);
  }

  private void flushToFile() {
    if (m_sb.length() == 0) return;

    p("Size " + m_sb.length() + ", flushing to " + m_file);
    FileWriter fw;
    try {
      fw = new FileWriter(m_file, true /* append */);
      fw.append(m_sb);
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    m_sb = new StringBuilder();
  }

  private static void p(String s) {
    System.out.println("[FileStringBuffer] " + s);
  }

  @Override
  public String toString() {
    flushToFile();
    String result = null;
    try {
      result = Files.readFile(m_file);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  public static void main(String[] args) {
    String s = "abcdefghijklmnopqrstuvwxyz";
    FileStringBuffer fsb = new FileStringBuffer(10);
    StringBuilder control = new StringBuilder();
    Random r = new Random();
    for (int i = 0; i < 4; i++) {
      int start = Math.abs(r.nextInt() % 26);
      int length = Math.abs(r.nextInt() % (26 - start));
      String fragment = s.substring(start, start + length);
      fragment = "abc";
      p("... Appending " + fragment);
      fsb.append(fragment);
      control.append(fragment);
    }

    Assert.assertEquals(fsb.toString(), control.toString());
  }
}
