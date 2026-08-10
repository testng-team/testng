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
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
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
    registerLexicalHandler(parser, dh);
    parser.parse(is, dh);
  }

  /**
   * Lets the handler observe the doctype declaration itself.
   *
   * <p>{@code SAXParser.parse(InputStream, DefaultHandler)} wires the content, error, DTD and
   * entity handlers, but not the lexical one, which has to be set as a property. Without it {@code
   * startDTD} is never delivered and an internal-subset doctype goes unnoticed.
   */
  private static void registerLexicalHandler(SAXParser parser, DefaultHandler dh) {
    if (!(dh instanceof LexicalHandler)) {
      return;
    }
    try {
      parser.setProperty("http://xml.org/sax/properties/lexical-handler", dh);
    } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
      // Optional in SAX. Losing it only means falling back to the previous behaviour for a
      // doctype with no external subset, so it is not worth failing the parse over.
      Logger.getLogger(XMLParser.class)
          .warn("The XML parser in use does not report doctype declarations: " + e);
    }
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
      // The value is deliberately discarded: this feature reports whether validation is currently
      // ON, not whether it is available. The JDK's Xerces answers false here and only turns true
      // after setValidating(true) -- and this runs before that call, so the answer is always the
      // default. Returning it would leave validation permanently off on a parser that supports it
      // perfectly well.
      //
      // Availability is signalled by the exception instead: SAXNotRecognizedException when the
      // feature name is unknown, SAXNotSupportedException when it is known but unavailable here.
      spf.getFeature("http://xml.org/sax/features/validation");
      return true;
    } catch (Exception ex) {
      Logger.getLogger(XMLParser.class)
          .warn("The XML parser in use does not support DTD validation: " + ex);
      return false;
    }
  }
}
