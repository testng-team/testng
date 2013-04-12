package org.testng.xml;

import org.testng.TestNGException;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SuiteXmlParser extends XMLParser<XmlSuite> {

  @Override
  public XmlSuite parse(String currentFile, InputStream inputStream, boolean loadClasses) {
    TestNGContentHandler contentHandler = new TestNGContentHandler(currentFile, loadClasses);

    try {
      parse(inputStream, contentHandler);

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
