package org.testng.reporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Files {

  public static String readFile(File f) throws IOException {
    return readFile(new FileInputStream(f));
  }

  public static String readFile(InputStream is) throws IOException {
    StringBuilder result = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = br.readLine();
    while (line != null) {
      result.append(line + "\n");
      line = br.readLine();
    }
    return result.toString();
  }

  public static void copyFile(InputStream from, File to) throws IOException {
    if (! to.getParentFile().exists()) {
      to.getParentFile().mkdirs();
    }

    OutputStream os = new FileOutputStream(to);
    byte[] buffer = new byte[65536];
    int count = from.read(buffer);
    while (count > 0) {
      os.write(buffer, 0, count);
      count = from.read(buffer);
    }
  }
}
