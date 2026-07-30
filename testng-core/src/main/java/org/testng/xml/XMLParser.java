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

  private static final AutoCloseableLock lock = new AutoCloseableLock();

  private static SAXParser m_saxParser;

  /** The mode {@link #m_saxParser} was configured for, so a change of mode can be noticed. */
  private static XmlValidationMode configuredFor;

  /** Whether {@link #m_saxParser} was built with DTD validation enabled. */
  private static boolean validating;

  /**
   * Whether the next parse will validate against the TestNG DTD. Exposed so that tests can tell
   * "validation is off in this JVM" apart from "this file is valid", instead of inferring it from a
   * parse that does not fail -- an inference that cannot be made.
   */
  static boolean isValidating() {
    try (AutoCloseableLock ignore = lock.lock()) {
      parser();
      return validating;
    }
  }

  public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
    try (AutoCloseableLock ignore = lock.lock()) {
      SAXParser parser = parser();
      if (parser == null) {
        throw new TestNGException("No SAXParser could be configured to read suite files.");
      }
      parser.parse(is, dh);
    }
  }

  /**
   * The shared parser, rebuilt when the validation mode has changed since it was created. The
   * parser is a singleton because it is expensive, but pinning it to the mode that happened to be
   * set when this class was first loaded made {@code testng.xml.validation} silently ineffective
   * for anything that sets it later -- the very failure mode this setting exists to fix.
   *
   * <p>Must be called while holding {@link #lock}.
   */
  private static SAXParser parser() {
    XmlValidationMode mode = XmlValidationMode.current();
    if (m_saxParser != null && mode == configuredFor) {
      return m_saxParser;
    }
    SAXParserFactory spf = loadSAXParserFactory();

    // Namespace awareness is deliberately left off: DTD validation does not need it, suite files
    // are not namespaced, and turning it on would make an unbound prefix fatal and an xmlns
    // attribute a validity error -- neither of which has anything to do with validating a suite.
    validating = mode.isValidating() && supportsValidation(spf);
    spf.setValidating(validating);
    try {
      m_saxParser = spf.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      Logger.getLogger(XMLParser.class).error(e.getMessage(), e);
      m_saxParser = null;
    }
    configuredFor = mode;
    return m_saxParser;
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
