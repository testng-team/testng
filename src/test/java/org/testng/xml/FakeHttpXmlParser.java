package org.testng.xml;

import org.testng.TestNGException;

import java.io.InputStream;

public class FakeHttpXmlParser implements ISuiteParser {
  @Override
  public boolean accept(String fileName) {
    return fileName.startsWith("https") || fileName.startsWith("http");
  }

  @Override
  public XmlSuite parse(String filePath, InputStream is, boolean loadClasses)
      throws TestNGException {
    XmlSuite suite = new XmlSuite();
    suite.setName("fake_suite");
    return suite;
  }
}
