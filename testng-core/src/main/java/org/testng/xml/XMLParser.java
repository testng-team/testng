package org.testng.xml;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.testng.TestNGException;
import org.testng.log4testng.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class XMLParser<T> implements IFileParser<T> {

  /**
   * Whether the next parse will validate against the TestNG DTD. Exposed so that tests can tell
   * "validation is off in this JVM" apart from "this file is valid", instead of inferring it from a
   * parse that does not fail -- an inference that cannot be made.
   */
  static boolean isValidating() {
    return configureValidation(loadSAXParserFactory());
  }

  /**
   * Each parse gets its own parser.
   *
   * <p>A single shared {@link SAXParser} was kept behind a class wide lock, which made every parse
   * in the JVM wait for every other one. That is not a matter of contention only: entity resolution
   * happens inside {@code parse}, and {@link TestNGContentHandler} resolves an unknown system id
   * over HTTP with no connect or read timeout, so one suite pointing at an unreachable DTD mirror
   * would block suite parsing everywhere until the socket gave up.
   *
   * <p>The parser was shared because building one was assumed to be expensive. Measured, {@code
   * SAXParserFactory.newInstance()} and {@code newSAXParser()} cost about 20 microseconds each --
   * nothing against reading a suite file, let alone against fetching a DTD. Building per parse also
   * removes the mutable static state that had to be invalidated whenever the validation mode
   * changed, so the mode in effect is now simply read at each parse.
   */
  public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
    SAXParserFactory spf = loadSAXParserFactory();
    configureValidation(spf);
    SAXParser parser;
    try {
      parser = spf.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      Logger.getLogger(XMLParser.class).error(e.getMessage(), e);
      throw new TestNGException("No SAXParser could be configured to read suite files.", e);
    }
    parser.parse(is, dh);
  }

  /**
   * Configures the factory for the validation mode currently in effect, and reports whether the
   * parsers it builds will validate. Pinning the mode to whatever was set when this class was first
   * loaded made {@code testng.xml.validation} silently ineffective for anything that sets it later
   * -- the very failure mode this setting exists to fix.
   */
  private static boolean configureValidation(SAXParserFactory spf) {
    // Namespace awareness is deliberately left off: DTD validation does not need it, suite files
    // are not namespaced, and turning it on would make an unbound prefix fatal and an xmlns
    // attribute a validity error -- neither of which has anything to do with validating a suite.
    boolean validating = XmlValidationMode.current().isValidating() && supportsValidation(spf);
    spf.setValidating(validating);
    return validating;
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
