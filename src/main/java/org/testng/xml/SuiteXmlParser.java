package org.testng.xml;

import org.testng.TestNGException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

public class SuiteXmlParser extends XMLParser<XmlSuite> implements ISuiteParser {

  @Override
  public XmlSuite parse(String currentFile, InputStream inputStream, boolean loadClasses) {
    TestNGContentHandler contentHandler = new TestNGContentHandler(currentFile, loadClasses);

    try {
      parse(inputStream, contentHandler);

      return contentHandler.getSuite();
    } catch (SAXException | IOException e) {
      throw new TestNGException(e);
    }
  }

  @Override
  public boolean accept(String fileName) {
    return fileName.endsWith(".xml");
  }
}
