package org.testng.reporters;

import org.testng.internal.Utils;

import java.io.File;
import java.io.IOException;

public class HtmlHelper {
  private static final String CSS_FILE_NAME = "testng.css";
  private static final String MY_CSS_FILE_NAME = "my-testng.css";

  static public String getCssString() {
    return getCssString("..");
  }

  static public String getCssString(String directory) {
    return
      "<link href=\"" + directory + "/" + CSS_FILE_NAME + "\" rel=\"stylesheet\" type=\"text/css\" />\n" +
      "<link href=\"" + directory + "/"+ MY_CSS_FILE_NAME + "\" rel=\"stylesheet\" type=\"text/css\" />\n";
  }

  public static File generateStylesheet(String outputDirectory) throws IOException {
    File stylesheetFile = new File(outputDirectory, CSS_FILE_NAME);
    if (!stylesheetFile.exists()) {
      Utils.writeResourceToFile(stylesheetFile, CSS_FILE_NAME, TestHTMLReporter.class);
    }
    return stylesheetFile;
  }
}
