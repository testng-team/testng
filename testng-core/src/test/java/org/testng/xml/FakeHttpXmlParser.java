package org.testng.xml;

import java.io.InputStream;
import org.jspecify.annotations.Nullable;
import org.testng.TestNGException;

public class FakeHttpXmlParser implements ISuiteParser {
  @Override
  public boolean accept(String fileName) {
    return fileName.startsWith("https") || fileName.startsWith("http");
  }

  @Override
  public XmlSuite parse(String filePath, @Nullable InputStream is, boolean loadClasses)
      throws TestNGException {
    XmlSuite suite = new XmlSuite();
    suite.setName("fake_suite");
    return suite;
  }
}
