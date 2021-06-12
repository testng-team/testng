package org.testng.xml;

import java.io.IOException;
import java.io.InputStream;
import org.testng.TestNGException;
import org.testng.xml.internal.Parser;
import org.xml.sax.SAXException;

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
    return Parser.hasFileScheme(fileName) && fileName.endsWith(".xml");
  }
}
