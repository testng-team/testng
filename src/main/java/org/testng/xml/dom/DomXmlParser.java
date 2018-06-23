package org.testng.xml.dom;

import org.testng.log4testng.Logger;
import org.testng.xml.ISuiteParser;
import org.testng.xml.Parser;
import org.testng.xml.XMLParser;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;

public class DomXmlParser extends XMLParser<XmlSuite> implements ISuiteParser {
  @Override
  public XmlSuite parse(String currentFile, InputStream inputStream, boolean loadClasses) {
    XmlSuite result = null;
    try {
      result = parse2(currentFile, inputStream, loadClasses);
    } catch (Exception e) {
      Logger.getLogger(DomXmlParser.class).error(e.getMessage(), e);
    }

    return result;
  }

  @Override
  public boolean accept(String fileName) {
    return Parser.hasFileScheme(fileName) && fileName.endsWith(".xml");
  }

  public XmlSuite parse2(String currentFile, InputStream inputStream, boolean loadClasses)
      throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // never forget this!
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(inputStream);

    DomUtil xpu = new DomUtil(doc);
    XmlSuite result = new XmlSuite();
    xpu.populate(result);

    System.out.println(result.toXml());
    return result;
  }
}
