package org.testng.reporters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class Files {

  private Files() {
    // defeat instantiation
  }

  public static String readFile(InputStream is) throws IOException {
    return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining("\n"));
  }
}
