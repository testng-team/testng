package org.testng.xml;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.testng.annotations.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The XSD half of "the corpus validates under both schemas".
 *
 * <p>{@code testng-1.1.xsd} mirrors {@code testng-1.1.dtd} declaration for declaration, so anything
 * the reader accepts must satisfy it, and so must anything the writer produces -- {@link
 * XmlRoundTripTest#serializedSuiteIsValidAgainstTheDtd} asserts the same thing against the DTD.
 *
 * <p>{@link #theXsdRejectsASuiteThatViolatesTheDtd()} is the one that gives the other two their
 * meaning: a schema that accepted everything would pass them both. DTD validation stayed silently
 * dead for years precisely because the tests around it only ever asserted that valid files parse.
 */
public class XsdValidationTest {

  /** Shipped next to the DTD, in {@code testng-core/src/main/resources}. */
  static final String TESTNG_XSD = "testng-1.1.xsd";

  private static final String INVALID_SUITE = "xml/validation/wrong-element-order.xml";

  private static final Schema SCHEMA = loadSchema();

  @Test(dataProvider = "suiteFiles", dataProviderClass = SuiteCorpus.class)
  public void everySuiteFileOfTheCorpusValidatesAgainstTheXsd(String suiteFile) throws Exception {
    List<String> violations = validateFile(suiteFile);

    assertThat(violations).as("%s must satisfy %s", suiteFile, TESTNG_XSD).isEmpty();
  }

  @Test(dataProvider = "suiteFiles", dataProviderClass = SuiteCorpus.class)
  public void serializedSuiteIsValidAgainstTheXsd(String suiteFile) throws Exception {
    String xml = SuiteCorpus.parseFile(suiteFile).toXml();

    List<String> violations = validateXml(xml);

    assertThat(violations)
        .as("the XML written for %s must satisfy %s:%n%s", suiteFile, TESTNG_XSD, xml)
        .isEmpty();
  }

  /**
   * The fixture is invalid because {@code <groups>} may only be the first child of {@code <suite>}.
   * Asserting on the message as well, so that the test cannot start passing for some unrelated
   * reason the day the fixture changes.
   */
  @Test
  public void theXsdRejectsASuiteThatViolatesTheDtd() throws Exception {
    List<String> violations = validateFile(INVALID_SUITE);

    assertThat(violations)
        .as("%s violates the DTD, so it must violate %s too", INVALID_SUITE, TESTNG_XSD)
        .isNotEmpty();
    assertThat(violations.toString()).contains("groups");
  }

  private static List<String> validateFile(String suiteFile) throws Exception {
    try (InputStream stream = SuiteCorpus.open(suiteFile)) {
      // No system id, for the same reason SuiteCorpus.open() has none: xml/issue2501/2501.xml
      // declares an external entity relative to the module directory.
      return validate(new InputSource(stream));
    }
  }

  private static List<String> validateXml(String xml) throws Exception {
    byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
    return validate(new InputSource(new ByteArrayInputStream(bytes)));
  }

  /**
   * Validates while parsing, with the schema attached to the parser, as {@code JUnitReportsTest}
   * does for the JUnit report schema.
   *
   * <p>Not through {@code Validator.validate(SAXSource)}: that implementation overwrites the entity
   * resolver of the reader it is handed with one of its own, so the doctype would be fetched from
   * testng.org instead of the classpath. Every suite file of the corpus declares one, and the
   * published {@code testng-1.0.dtd} still has the {@code junit} attribute that 1.1 dropped, so the
   * corpus failed to validate against a schema that was in fact correct.
   *
   * <p>The doctype must still be processed rather than ignored: it expands the external entity of
   * {@code xml/issue2501/2501.xml} and supplies the defaulted attributes.
   */
  private static List<String> validate(InputSource source) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Schema validation is defined in terms of namespaces, even for a schema without one.
    factory.setNamespaceAware(true);
    factory.setSchema(SCHEMA);

    List<String> violations = new ArrayList<>();
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(SuiteCorpus.bundledDtdResolver());
    builder.setErrorHandler(collectInto(violations));
    builder.parse(source);
    return violations;
  }

  private static ErrorHandler collectInto(List<String> violations) {
    return new ErrorHandler() {
      @Override
      public void warning(SAXParseException e) {
        violations.add(e.getMessage());
      }

      @Override
      public void error(SAXParseException e) {
        violations.add(e.getMessage());
      }

      @Override
      public void fatalError(SAXParseException e) throws SAXException {
        throw e;
      }
    };
  }

  /** Fails loudly when the schema is missing, rather than turning every test below into a no-op. */
  private static Schema loadSchema() {
    URL url = XsdValidationTest.class.getClassLoader().getResource(TESTNG_XSD);
    Objects.requireNonNull(url, TESTNG_XSD + " is not on the test classpath");
    try {
      return SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI)
          .newSchema(new StreamSource(url.toExternalForm()));
    } catch (SAXException e) {
      throw new IllegalStateException(TESTNG_XSD + " does not compile", e);
    }
  }
}
