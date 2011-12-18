package org.testng.reporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Files {

  public static String readFile(File f) throws IOException {
    StringBuilder result = new StringBuilder();
    BufferedReader br = new BufferedReader(new FileReader(f));
    String line = br.readLine();
    while (line != null) {
      result.append(line + "\n");
      line = br.readLine();
    }
    return result.toString();
  }
}
