package org.testng;

import java.io.IOException;

/**
 * Converts XML suite files to YAML and YAML suite files to XML.
 *
 * @deprecated Use {@link org.testng.cli.jcommander.Converter} instead.
 */
@Deprecated
public class Converter {
  public static void main(String[] args) throws IOException {
    org.testng.cli.jcommander.Converter.main(args);
  }
}
