package org.testng.internal;

import org.testng.TestNGException;
import org.testng.xml.ISuiteParser;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class YamlParser implements ISuiteParser {

  @Override
  public XmlSuite parse(String filePath, InputStream is, boolean loadClasses)
      throws TestNGException {
    try {
      return Yaml.parse(filePath, is);
    } catch (FileNotFoundException e) {
      throw new TestNGException(e);
    }
  }

  @Override
  public boolean accept(String fileName) {
    return Parser.hasFileScheme(fileName) && fileName.endsWith(".yaml");
  }
}
