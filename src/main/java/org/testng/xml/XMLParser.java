package org.testng.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.testng.TestNGException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

abstract public class XMLParser<T> implements IFileParser<T> {

  private final static SAXParser m_saxParser;

  static {
    SAXParserFactory spf = loadSAXParserFactory();

    if (supportsValidation(spf)) {
      spf.setNamespaceAware(true);
      spf.setValidating(true);
    }

    SAXParser parser = null;
    try {
      parser = spf.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      e.printStackTrace();
    }
    m_saxParser = parser;
  }

  public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
    synchronized (m_saxParser) {
      m_saxParser.parse(is, dh);
    }
  }

  /**
   * Tries to load a <code>SAXParserFactory</code> via <code>SAXParserFactory.newInstance()</code>.
   *
   * @return a <code>SAXParserFactory</code> implementation
   * @throws TestNGException thrown if no <code>SAXParserFactory</code> can be loaded
   */
  private static SAXParserFactory loadSAXParserFactory() {

    try {
      return SAXParserFactory.newInstance();
    } catch (FactoryConfigurationError fcerr) {
      throw new TestNGException("Cannot initialize a SAXParserFactory. Root cause: " + fcerr.getMessage(), fcerr);
    }
  }


  /**
   * Tests if the current <code>SAXParserFactory</code> supports DTD validation.
   */
  private static boolean supportsValidation(SAXParserFactory spf) {
    try {
      spf.getFeature("http://xml.org/sax/features/validation");
      return true;
    }
    catch(Exception ex) {
      return false;
    }
  }

//  private static void ppp(String s) {
//    System.out.println("[Parser] " + s);
//  }

//  /**
//   *
//   * @param argv ignored
//   * @throws FileNotFoundException if the
//   * @throws ParserConfigurationException
//   * @throws SAXException
//   * @throws IOException
//   * @since 1.0
//   */
//  public static void main(String[] argv)
//    throws FileNotFoundException, ParserConfigurationException, SAXException, IOException
//  {
//    XmlSuite l =
//      new Parser("c:/eclipse-workspace/testng/test/testng.xml").parse();
//
//    System.out.println(l);
//  }  @Override

}
