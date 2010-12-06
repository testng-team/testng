package org.testng.xml;

import org.testng.TestNGException;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SuiteXmlParser extends XmlParser {

  @Override
  public XmlSuite parse(String currentFile, InputStream inputStream) {
    TestNGContentHandler contentHandler = new TestNGContentHandler(currentFile);

    try {
      m_saxParser.parse(inputStream, contentHandler);

      return contentHandler.getSuite();
    }
    catch (FileNotFoundException e) {
      throw new TestNGException(e);
    } catch (SAXException e) {
      throw new TestNGException(e);
    } catch (IOException e) {
      throw new TestNGException(e);
    }
  }


}
