package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static test.SimpleBaseTest.getPathToResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;

/**
 * Proves that DTD validation is actually wired into the parser.
 *
 * <p>Validation was silently dead: {@code XMLParser} probed the SAX validation feature under an
 * {@code https} identifier, no parser recognized it, and {@code setValidating(true)} was never
 * reached. A test asserting only that valid files parse would have passed throughout, so the check
 * that matters is that an <em>invalid</em> file is rejected.
 */
public class XmlValidationTest {

  private static final String INVALID_SUITE = "xml/validation/wrong-element-order.xml";
  private static final String VALID_SUITE = "xml/goodWithDoctype.xml";

  @AfterMethod(alwaysRun = true)
  public void clearValidationMode() {
    System.clearProperty(RuntimeBehavior.XML_VALIDATION_MODE);
  }

  @Test
  public void strictModeRejectsASuiteThatViolatesTheDtd() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "strict");

    assertThatThrownBy(() -> parse(INVALID_SUITE))
        .hasMessageContaining("must match")
        .hasMessageContaining("suite");
  }

  @Test
  public void warnModeAcceptsASuiteThatViolatesTheDtd() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "warn");

    assertThatCode(() -> assertThat(parse(INVALID_SUITE).getName()).isEqualTo("WrongElementOrder"))
        .doesNotThrowAnyException();
  }

  @Test
  public void strictModeAcceptsAValidSuite() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "strict");

    assertThatCode(() -> assertThat(parse(VALID_SUITE).getName()).isEqualTo("GitHub809"))
        .doesNotThrowAnyException();
  }

  @Test
  public void anUnknownModeFallsBackToWarn() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "not-a-mode");

    assertThat(XmlValidationMode.current()).isEqualTo(XmlValidationMode.WARN);
  }

  @Test
  public void theDefaultModeIsWarn() {
    System.clearProperty(RuntimeBehavior.XML_VALIDATION_MODE);

    assertThat(XmlValidationMode.current()).isEqualTo(XmlValidationMode.WARN);
    assertThat(XmlValidationMode.WARN.isValidating()).isTrue();
    assertThat(XmlValidationMode.OFF.isValidating()).isFalse();
  }

  private static XmlSuite parse(String suiteFile) throws IOException {
    try (InputStream stream = Files.newInputStream(Paths.get(getPathToResource(suiteFile)))) {
      return new SuiteXmlParser().parse(suiteFile, stream, false);
    }
  }
}
