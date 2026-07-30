package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static test.SimpleBaseTest.getPathToResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import javax.xml.parsers.SAXParserFactory;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;
import org.testng.xml.internal.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * Proves that DTD validation is actually wired into the parser.
 *
 * <p>Validation was silently dead: {@code XMLParser} probed the SAX validation feature under an
 * {@code https} identifier, no parser recognized it, and {@code setValidating(true)} was never
 * reached. A test asserting only that valid files parse would have passed throughout, so the check
 * that matters is that an <em>invalid</em> file is rejected.
 *
 * <p>The two halves of the wiring are asserted separately on purpose. Whether {@code XMLParser}
 * validates is read back directly rather than inferred from a parse: inferring it made this test
 * fail intermittently across the CI matrix, because "no error was reported" and "validation is not
 * enabled" look identical from the outside. How a violation is <em>reported</em> is exercised
 * through a parser built here, so it cannot depend on the state of the shared one.
 */
public class XmlValidationTest {

  private static final String INVALID_SUITE = "xml/validation/wrong-element-order.xml";
  private static final String VALID_SUITE = "xml/goodWithDoctype.xml";

  private String previousMode;

  @BeforeMethod
  public void rememberValidationMode() {
    previousMode = System.getProperty(RuntimeBehavior.XML_VALIDATION_MODE);
  }

  /**
   * Restores rather than clears: the property is global, so clearing it unconditionally would
   * discard a value the JVM was started with -- the build forwards every {@code testng.*} property
   * into the test JVM -- and leak into the rest of the suite.
   */
  @AfterMethod(alwaysRun = true)
  public void restoreValidationMode() {
    if (previousMode == null) {
      System.clearProperty(RuntimeBehavior.XML_VALIDATION_MODE);
    } else {
      System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, previousMode);
    }
  }

  /**
   * The wiring that was broken. Asserted directly instead of through a parse, so that a JVM which
   * cannot validate says so rather than looking like a suite file that happens to be valid.
   */
  @Test
  public void theSharedParserValidatesSuiteFilesByDefault() {
    if (XmlValidationMode.current() == XmlValidationMode.OFF) {
      throw new SkipException(
          "the JVM is configured with -D"
              + RuntimeBehavior.XML_VALIDATION_MODE
              + "=off, which is a supported way to run the suite");
    }

    assertThat(XMLParser.isValidating())
        .as(
            "the shared SAXParser must validate; if this fails, the JAXP implementation on the"
                + " classpath does not support DTD validation")
        .isTrue();
  }

  /** Turning validation off must actually reach the parser, not only the reporting. */
  @Test
  public void offModeStopsTheSharedParserFromValidating() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "off");

    assertThat(XMLParser.isValidating()).isFalse();
  }

  @Test
  public void strictModeRejectsASuiteThatViolatesTheDtd() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "strict");

    assertThatThrownBy(() -> parseValidating(INVALID_SUITE)).isInstanceOf(SAXParseException.class);
  }

  @Test
  public void strictModeAcceptsAValidSuite() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "strict");

    assertThatCode(() -> assertThat(parseValidating(VALID_SUITE).getName()).isEqualTo("GitHub809"))
        .doesNotThrowAnyException();
  }

  @Test
  public void warnModeAcceptsASuiteThatViolatesTheDtd() {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "warn");

    assertThatCode(
            () ->
                assertThat(parseValidating(INVALID_SUITE).getName()).isEqualTo("WrongElementOrder"))
        .doesNotThrowAnyException();
  }

  /**
   * A violation must be reported even when the DTD is not the copy TestNG substitutes for its own
   * doctype URLs -- an air-gapped or mirrored setup ships the DTD next to the suite. Reporting used
   * to be gated on "TestNG provided the DTD", so those users got no validation at all, even under
   * strict.
   *
   * <p>Written to a temporary directory rather than checked in, so that the DTD used here cannot
   * drift from the one TestNG ships.
   */
  @Test
  public void strictModeAlsoRejectsWhenTheSuitePointsAtItsOwnDtd() throws Exception {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, "strict");
    Path directory = Files.createTempDirectory("testng-local-dtd");
    Path dtd = directory.resolve(Parser.TESTNG_DTD);
    Path suite = directory.resolve("local-dtd-wrong-order.xml");
    try {
      try (InputStream shipped =
          getClass().getClassLoader().getResourceAsStream(Parser.TESTNG_DTD)) {
        Files.copy(Objects.requireNonNull(shipped, "the DTD must be on the classpath"), dtd);
      }
      Files.write(
          suite,
          ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                  + "<!DOCTYPE suite SYSTEM \""
                  + Parser.TESTNG_DTD
                  + "\">\n"
                  // <groups> may only be the first child of <suite>.
                  + "<suite name=\"LocalDtdWrongOrder\">\n"
                  + "  <parameter name=\"before\" value=\"groups\"/>\n"
                  + "  <groups><run><include name=\"included\"/></run></groups>\n"
                  + "</suite>\n")
              .getBytes(StandardCharsets.UTF_8));

      assertThatThrownBy(() -> parseValidating(suite)).isInstanceOf(SAXParseException.class);
    } finally {
      // The suite runs in one fork per two cores, so leaking a directory holding a copy of the
      // DTD on every build adds up.
      Files.deleteIfExists(suite);
      Files.deleteIfExists(dtd);
      Files.deleteIfExists(directory);
    }
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

  /**
   * {@code toUpperCase()} without a locale maps 'i' to 'İ' in Turkish, which turned {@code strict}
   * into an unknown value and silently degraded it to the default. The CI matrix has a {@code
   * tr_TR} axis, so this was a live failure rather than a theoretical one.
   *
   * <p>Mutating the default locale is process wide; it is restored in a {@code finally} and the
   * enclosing {@code <test>} of testng.xml runs single threaded.
   */
  @Test
  public void theModeIsParsedIndependentlyOfTheDefaultLocale() {
    Locale previousLocale = Locale.getDefault();
    Locale.setDefault(new Locale("tr", "TR"));
    try {
      assertThat(modeOf("strict")).isEqualTo(XmlValidationMode.STRICT);
      assertThat(modeOf("STRICT")).isEqualTo(XmlValidationMode.STRICT);
      assertThat(modeOf("  Strict  ")).isEqualTo(XmlValidationMode.STRICT);
      assertThat(modeOf("off")).isEqualTo(XmlValidationMode.OFF);
      assertThat(modeOf("warn")).isEqualTo(XmlValidationMode.WARN);
    } finally {
      Locale.setDefault(previousLocale);
    }
  }

  private static XmlValidationMode modeOf(String spelling) {
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, spelling);
    return XmlValidationMode.current();
  }

  /**
   * Parses with a validating parser created here rather than with {@link SuiteXmlParser}, whose
   * parser is a JVM-wide singleton configured once at class-initialisation time. Only the reporting
   * path is under test; {@link #theSharedParserValidatesSuiteFilesByDefault()} covers the
   * singleton.
   */
  private static XmlSuite parseValidating(String suiteFile) throws Exception {
    return parseValidating(Paths.get(getPathToResource(suiteFile)));
  }

  private static XmlSuite parseValidating(Path path) throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    TestNGContentHandler handler = new TestNGContentHandler(path.toString(), false);
    try (InputStream stream = Files.newInputStream(path)) {
      InputSource source = new InputSource(stream);
      // The system id matters: without it a relative doctype cannot be resolved, so TestNG falls
      // back to substituting its own DTD and the "suite points at its own DTD" case would silently
      // exercise the substituted path instead.
      source.setSystemId(path.toUri().toString());
      factory.newSAXParser().parse(source, handler);
    }
    return handler.getSuite();
  }
}
