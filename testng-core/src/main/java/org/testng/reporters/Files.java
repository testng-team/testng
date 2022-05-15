package org.testng.reporters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class Files {

  private Files() {
    // defeat instantiation
  }

  /** @deprecated - This method stands deprecated as of TestNG <code>7.6.0</code> */
  @Deprecated
  public static String readFile(File f) throws IOException {
    try (InputStream is = new FileInputStream(f)) {
      return readFile(is);
    }
  }

  public static String readFile(InputStream is) throws IOException {
    return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining("\n"));
  }

  /** @deprecated - This method stands deprecated as of TestNG <code>7.6.0</code> */
  @Deprecated
  public static void writeFile(String string, File f) throws IOException {
    f.getParentFile().mkdirs();
    try (FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw)) {
      bw.write(string);
    }
  }

  /** @deprecated - This method stands deprecated as of TestNG <code>7.6.0</code> */
  @Deprecated
  public static void copyFile(InputStream from, File to) throws IOException {
    if (!to.getParentFile().exists()) {
      to.getParentFile().mkdirs();
    }

    try (OutputStream os = new FileOutputStream(to)) {
      byte[] buffer = new byte[65536];
      int count = from.read(buffer);
      while (count > 0) {
        os.write(buffer, 0, count);
        count = from.read(buffer);
      }
    }
  }

  /** @deprecated - This method stands deprecated as of TestNG <code>7.6.0</code> */
  @Deprecated
  public static String streamToString(InputStream is) throws IOException {
    if (is != null) {
      Writer writer = new StringWriter();

      char[] buffer = new char[1024];
      try (Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
        int n;
        while ((n = reader.read(buffer)) != -1) {
          writer.write(buffer, 0, n);
        }
      }
      return writer.toString();
    } else {
      return "";
    }
  }
}
