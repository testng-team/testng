package org.testng.internal;

import java.io.FileNotFoundException;
import java.io.InputStream;
import org.jspecify.annotations.Nullable;
import org.testng.TestNGException;
import org.testng.xml.ISuiteParser;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;

public class YamlParser implements ISuiteParser {

  @Override
  public XmlSuite parse(String filePath, @Nullable InputStream is, boolean loadClasses)
      throws TestNGException {
    try {
      return Yaml.parse(filePath, is, loadClasses);
    } catch (FileNotFoundException e) {
      throw new TestNGException(e);
    }
  }

  @Override
  public boolean accept(String fileName) {
    return Parser.hasFileScheme(fileName)
        && (fileName.endsWith(".yaml") || fileName.endsWith(".yml"));
  }
}
