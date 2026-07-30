package org.testng.xml;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.testng.TestNGException;
import org.testng.internal.AutoCloseableLock;
import org.testng.log4testng.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class XMLParser<T> implements IFileParser<T> {

  private static final SAXParser m_saxParser;

  /**
   * Whether the shared parser was built with DTD validation enabled. Decided once, because the
   * parser itself is a singleton, and exposed so that tests can tell "validation is off in this
   * JVM" apart from "this file is valid" instead of inferring it from a parse that does not fail.
   */
  private static final boolean validating;

  static {
    SAXParserFactory spf = loadSAXParserFactory();

    // Namespace awareness is deliberately left off: DTD validation does not need it, suite files
    // are not namespaced, and turning it on would make an unbound prefix fatal and an xmlns
    // attribute a validity error -- neither of which has anything to do with validating a suite.
    validating = XmlValidationMode.current().isValidating() && supportsValidation(spf);
    spf.setValidating(validating);

    SAXParser parser = null;
    try {
      parser = spf.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      Logger.getLogger(XMLParser.class).error(e.getMessage(), e);
    }
    m_saxParser = parser;
  }

  private static final AutoCloseableLock lock = new AutoCloseableLock();

  /** Whether the shared parser validates suite files against the TestNG DTD. */
  static boolean isValidating() {
    return validating;
  }

  public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
    try (AutoCloseableLock ignore = lock.lock()) {
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
      throw new TestNGException(
          "Cannot initialize a SAXParserFactory. Root cause: " + fcerr.getMessage(), fcerr);
    }
  }

  /**
   * Tests if the current <code>SAXParserFactory</code> supports DTD validation.
   *
   * <p>The feature name is a plain identifier, not a URL to dereference, so it keeps its historical
   * <code>http</code> scheme. Probing it under <code>https</code> makes every conforming parser
   * raise <code>SAXNotRecognizedException</code>, which silently disabled validation altogether.
   */
  private static boolean supportsValidation(SAXParserFactory spf) {
    try {
      spf.getFeature("http://xml.org/sax/features/validation");
      return true;
    } catch (Exception ex) {
      Logger.getLogger(XMLParser.class)
          .warn("The XML parser in use does not support DTD validation: " + ex);
      return false;
    }
  }
}
