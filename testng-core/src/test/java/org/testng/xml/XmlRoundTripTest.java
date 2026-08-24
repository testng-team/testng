package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.testng.xml.SuiteCorpus.parseFile;
import static org.testng.xml.SuiteCorpus.parseString;

import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;

/**
 * Characterization tests over every suite file of the test corpus, pinning the behaviour of the XML
 * reader ({@link SuiteXmlParser}) and of the XML writer ({@code toXml()}) as a pair.
 *
 * <p>These tests assert nothing about what the output <em>should</em> look like; they assert that
 * it does not change. They exist so that moving the serialization code out of the domain model can
 * be done safely, since the project has no binary-compatibility tooling in CI.
 *
 * <p>Two independent invariants are checked, because neither one alone is sufficient: the
 * serialized form must be a fixed point, which pins attribute selection and layout, and the parsed
 * model must survive unchanged, which pins the data (see {@link SuiteDigest}).
 */
public class XmlRoundTripTest {

  private @Nullable String previousMode;

  @BeforeMethod
  public void rememberValidationMode() {
    previousMode = System.getProperty(RuntimeBehavior.XML_VALIDATION_MODE);
  }

  /** Restores rather than clears, for the reason given on {@code XmlValidationTest}. */
  @AfterMethod(alwaysRun = true)
  public void restoreValidationMode() {
    if (previousMode == null) {
      System.clearProperty(RuntimeBehavior.XML_VALIDATION_MODE);
    } else {
      System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, previousMode);
    }
  }

  @Test(dataProvider = "suiteFiles", dataProviderClass = SuiteCorpus.class)
  public void serializedSuiteIsAFixedPoint(String suiteFile) throws IOException {
    String firstPass = parseFile(suiteFile).toXml();
    String secondPass = parseString(suiteFile, firstPass).toXml();

    assertThat(secondPass)
        .as("re-serializing the suite parsed back from %s must be a fixed point", suiteFile)
        .isEqualTo(firstPass);
  }

  @Test(dataProvider = "suiteFiles", dataProviderClass = SuiteCorpus.class)
  public void suiteContentSurvivesTheRoundTrip(String suiteFile) throws IOException {
    XmlSuite parsedFromFile = parseFile(suiteFile);
    XmlSuite reparsed = parseString(suiteFile, parsedFromFile.toXml());

    assertThat(SuiteDigest.of(reparsed))
        .as(
            "the suite parsed back from the serialized form of %s must carry the same data",
            suiteFile)
        .isEqualTo(SuiteDigest.of(parsedFromFile));
  }

  /**
   * What we write must satisfy the grammar we declare. Without this, {@code
   * testng.xml.validation=strict} would reject TestNG's own output -- {@code testng-failed.xml} is
   * produced by {@code toXml()} -- and the corpus round trip above would happily re-parse invalid
   * XML because the default mode only warns.
   *
   * <p>Asserted by re-reading the output through {@link SuiteXmlParser} in strict mode rather than
   * against a schema this test picks. Which grammar applies is now the reader's decision, taken
   * from what the document declares, so validating against a schema chosen here would assert the
   * test's own assumption instead of the behaviour users get. {@code
   * XsdValidationTest#serializedSuiteIsValidAgainstTheXsd} covers the other half, against the
   * schema directly.
   */
  @Test(dataProvider = "suiteFiles", dataProviderClass = SuiteCorpus.class)
  public void serializedSuiteIsAcceptedInStrictMode(String suiteFile) throws Exception {
    String xml = parseFile(suiteFile).toXml();
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "strict");

    assertThatCode(() -> parseString(suiteFile, xml))
        .as(
            "the XML written for %s must be re-readable under strict validation:%n%s",
            suiteFile, xml)
        .doesNotThrowAnyException();
  }

  /**
   * The schema we declare must be the one the reader resolves. The writer lives in testng-core-api
   * and the reader in testng-core, so they cannot share a constant and had silently drifted apart
   * once already, when the doctype advertised 1.0 while the parser resolved 1.1. A comment would
   * not have caught that; this does.
   */
  @Test
  public void theEmittedSchemaDeclarationNamesTheSchemaTheParserResolves() {
    assertThat(new XmlSuite().toXml())
        .contains("xsi:noNamespaceSchemaLocation")
        .contains(XMLParser.TESTNG_XSD);
  }

  /**
   * A doctype and a schema declaration cannot both be emitted: the DTD declares neither {@code
   * xmlns:xsi} nor {@code xsi:noNamespaceSchemaLocation}, so a document carrying both is not
   * DTD-valid. Pinned because emitting the doctype "as well, just in case" is the obvious thing to
   * try, and the resulting file is rejected by the very validation it was meant to satisfy.
   */
  @Test
  public void theOutputDeclaresOneGrammarAndNotTwo() {
    assertThat(new XmlSuite().toXml()).doesNotContain("<!DOCTYPE");
  }
}
