package org.testng.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.testng.TestNGException;
import org.testng.log4testng.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public abstract class XMLParser<T> implements IFileParser<T> {

  /** The name of the TestNG schema, shipped next to the DTD. */
  public static final String TESTNG_XSD = "testng-1.1.xsd";

  /** Where the schema is published, for suite files to declare and for tools to fetch. */
  public static final String HTTPS_TESTNG_XSD_URL = "https://testng.org/" + TESTNG_XSD;

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
    if (!validatesAgainstSuiteSchema() || !XmlValidationMode.current().isValidating()) {
      // Nothing to choose between, so neither the buffering nor the prologue scan below would be
      // read. This is also the path every parser that is not reading a suite takes.
      configureValidation(spf);
      parse(spf, is, dh, false, false);
      return;
    }
    // Buffered because the grammar has to be chosen before the parser is built -- JAXP rejects
    // setValidating(true) together with setSchema(...) -- while the doctype is only observed once
    // parsing is under way. Suite files are a few kilobytes; #3316 buffers the DTD on the same
    // grounds.
    //
    // Closed here rather than left to the parser, which is what consumes -- and closes -- the
    // stream on the path below. Parser opens a FileInputStream per suite file and closes none, and
    // JarFileUtils deletes the directory it extracted a jar into as soon as parsing returns: on
    // Windows a file still held open cannot be deleted.
    byte[] document;
    try (InputStream source = is) {
      document = source.readAllBytes();
    }
    boolean declaresDoctype = XmlPrologue.declaresDoctype(document);
    boolean schemaValidated = configureGrammar(spf, declaresDoctype);
    parse(spf, new ByteArrayInputStream(document), dh, schemaValidated, declaresDoctype);
  }

  private static void parse(
      SAXParserFactory spf,
      InputStream is,
      DefaultHandler dh,
      boolean schemaValidated,
      boolean declaresDoctype)
      throws SAXException, IOException {
    SAXParser parser;
    try {
      parser = spf.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      Logger.getLogger(XMLParser.class).error(e.getMessage(), e);
      throw new TestNGException("No SAXParser could be configured to read suite files.", e);
    }
    registerLexicalHandler(parser, dh);
    if (dh instanceof TestNGContentHandler) {
      TestNGContentHandler handler = (TestNGContentHandler) dh;
      handler.setSchemaValidated(schemaValidated);
      if (declaresDoctype) {
        // Only ever confirms: the scan answers "no doctype" for input it could not read, so a
        // false here means "nothing found", not "nothing there".
        handler.doctypeDeclared();
      }
    }
    parser.parse(is, dh);
  }

  /**
   * Whether a document with no doctype should be validated against the TestNG suite schema.
   *
   * <p>False here, and overridden only by {@link SuiteXmlParser}: this class is a generic SAX front
   * end -- {@code TestsuiteXmlParser} reads JUnit reports through it -- and a {@code <testsuite>}
   * document is not a suite. Validating one against {@code testng-1.1.xsd} would report violations
   * for a grammar it was never meant to satisfy.
   */
  protected boolean validatesAgainstSuiteSchema() {
    return false;
  }

  /**
   * Points the parser at the grammar the document declares, and reports whether that grammar is the
   * schema rather than the DTD.
   *
   * <p>A suite carrying a doctype keeps being validated against it, so nothing changes for the
   * documents that have one -- including the case of an internal subset that declares entities but
   * not {@code <suite>}, which a DTD-validating parser rightly rejects and a schema would not see.
   * A suite that declares no doctype had no grammar at all until now, and gets the bundled schema.
   */
  private static boolean configureGrammar(SAXParserFactory spf, boolean declaresDoctype) {
    if (declaresDoctype) {
      configureValidation(spf);
      return false;
    }
    if (BundledSchema.INSTANCE == null) {
      return false;
    }
    // Schema validation is defined in terms of namespaces, even for a schema without one. Safe to
    // turn on here in a way it is not for the DTD path: with no DTD to validate against, an xmlns
    // attribute cannot become a validity error for being undeclared.
    spf.setNamespaceAware(true);
    spf.setSchema(BundledSchema.INSTANCE);
    return true;
  }

  /**
   * The bundled schema, compiled once and only when a suite is actually read without a doctype.
   *
   * <p>Loaded from the classpath rather than from the document's {@code
   * xsi:noNamespaceSchemaLocation}, for the reason the entity resolver loads the DTD from the
   * classpath: a parse must not depend on reaching testng.org, and must not fetch a grammar from
   * whatever URL a suite file happens to name. The declaration in the document stays meaningful to
   * everything else -- IDEs and standalone validators read it -- but TestNG validates against the
   * copy it ships.
   *
   * <p>A missing or uncompilable schema degrades to "no validation" rather than failing the parse:
   * a repackaged jar that dropped the resource should still run suites.
   */
  private static final class BundledSchema {
    private static final Schema INSTANCE = compile();

    private static Schema compile() {
      URL url = XMLParser.class.getClassLoader().getResource(TESTNG_XSD);
      if (url == null) {
        Logger.getLogger(XMLParser.class)
            .warn(
                TESTNG_XSD
                    + " is not on the classpath; suites without a doctype are not validated.");
        return null;
      }
      try {
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            .newSchema(new StreamSource(url.toExternalForm()));
      } catch (SAXException e) {
        Logger.getLogger(XMLParser.class).warn(TESTNG_XSD + " does not compile: " + e);
        return null;
      }
    }
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
      // Optional in SAX, and no longer costly for a suite: the prologue scan above already told
      // the handler whether a doctype is declared, including the internal-subset-only case that
      // neither startDTD nor resolveEntity would report here. Not worth failing the parse over.
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
