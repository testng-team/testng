package org.testng.xml.dom;

import org.testng.xml.ISuiteParser;
import org.testng.xml.XMLParser;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import java.io.IOException;
import java.io.InputStream;

public class DomXmlParser extends XMLParser<XmlSuite> implements ISuiteParser {
  @Override
  public XmlSuite parse(String currentFile, InputStream inputStream, boolean loadClasses) {
    XmlSuite result = null;
    try {
      result = parse2(currentFile, inputStream, loadClasses);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  @Override
  public boolean accept(String fileName) {
    return fileName.endsWith(".xml");
  }

  public XmlSuite parse2(String currentFile, InputStream inputStream,
      boolean loadClasses) throws ParserConfigurationException, SAXException,
      IOException, XPathExpressionException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // never forget this!
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(inputStream);

    DomUtil xpu = new DomUtil(doc);
    XmlSuite result = new XmlSuite();
    xpu.populate(result);
//    XPathFactory xpathFactory = XPathFactory.newInstance();
//    XPath xpath = xpathFactory.newXPath();
//
//    {
//      XPathExpression expr = xpath.compile("//suite");
//      Object result = expr.evaluate(doc, XPathConstants.NODESET);
//      NodeList nodes = (NodeList) result;
//      for (int i = 0; i < nodes.getLength(); i++) {
//        Node node = nodes.item(i);
//        for (int j = 0; j < node.getAttributes().getLength(); j++) {
//          System.out.println(node.getAttributes().item(j));
//        }
//      }
//    }

//    {
//      XPathExpression expr = xpath.compile("//suite/@name");
//      Object result = expr.evaluate(doc, XPathConstants.STRING);
//      System.out.println("NAME:" + result);
//    }
    System.out.println(result.toXml());
    return result;
  }
}
