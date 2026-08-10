package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static test.SimpleBaseTest.getPathToResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.SAXParserFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.internal.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

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

  @Test(dataProvider = "suiteFiles")
  public void serializedSuiteIsAFixedPoint(String suiteFile) throws IOException {
    String firstPass = parseFile(suiteFile).toXml();
    String secondPass = parseString(suiteFile, firstPass).toXml();

    assertThat(secondPass)
        .as("re-serializing the suite parsed back from %s must be a fixed point", suiteFile)
        .isEqualTo(firstPass);
  }

  @Test(dataProvider = "suiteFiles")
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
   * What we write must satisfy the DTD we advertise. Without this, {@code
   * testng.xml.validation=strict} would reject TestNG's own output -- {@code testng-failed.xml} is
   * produced by {@code toXml()} -- and the corpus round trip above would happily re-parse invalid
   * XML because the default mode only warns.
   */
  @Test(dataProvider = "suiteFiles")
  public void serializedSuiteIsValidAgainstTheDtd(String suiteFile) throws Exception {
    String xml = parseFile(suiteFile).toXml();

    List<String> violations = validateAgainstDtd(xml);

    assertThat(violations)
        .as("the XML written for %s must satisfy %s:%n%s", suiteFile, Parser.TESTNG_DTD, xml)
        .isEmpty();
  }

  /**
   * Validates against the bundled DTD, resolving it locally so the test never touches the network.
   */
  private static List<String> validateAgainstDtd(String xml) throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    List<String> violations = new ArrayList<>();
    factory
        .newSAXParser()
        .parse(
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
            new DefaultHandler() {
              @Override
              public InputSource resolveEntity(String publicId, String systemId) {
                return new InputSource(
                    XmlRoundTripTest.class.getClassLoader().getResourceAsStream(Parser.TESTNG_DTD));
              }

              @Override
              public void error(SAXParseException e) {
                violations.add(e.getMessage());
              }
            });
    return violations;
  }

  /**
   * The doctype we write must name the DTD the reader resolves. The writer lives in testng-core-api
   * and the reader in testng-core, so they cannot share a constant and had silently drifted apart
   * (1.0 written, 1.1 resolved). A comment would not have caught that; this does.
   */
  @Test
  public void theEmittedDoctypeNamesTheDtdTheParserResolves() {
    assertThat(new XmlSuite().toXml()).contains(Parser.TESTNG_DTD);
  }

  /**
   * Fixtures that exist precisely because they are not valid, so they cannot be round tripped: with
   * {@code testng.xml.validation=strict} the very first parse throws, which is what they are for.
   */
  private static final String INVALID_ON_PURPOSE = "xml" + File.separator + "validation";

  /**
   * Every {@code .xml} file of the test corpus whose content contains a {@code <suite} start tag.
   *
   * <p>The filter is deliberately content based rather than name based, so that suite files added
   * later are picked up without touching this class. It also excludes, without needing an explicit
   * list, the fixtures whose root element is {@code <Suite>} on purpose ({@code xml/badWith*.xml}).
   */
  @DataProvider(name = "suiteFiles")
  public static Object[][] suiteFiles() throws IOException {
    Path root = Paths.get(getPathToResource(""));
    try (Stream<Path> paths = Files.walk(root)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".xml"))
          .filter(XmlRoundTripTest::declaresASuite)
          .sorted()
          .map(path -> root.relativize(path).toString())
          .filter(relativePath -> !relativePath.startsWith(INVALID_ON_PURPOSE))
          .map(relativePath -> new Object[] {relativePath})
          .toArray(Object[][]::new);
    }
  }

  private static boolean declaresASuite(Path path) {
    try {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8).contains("<suite");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static XmlSuite parseFile(String suiteFile) throws IOException {
    Path path = Paths.get(getPathToResource(suiteFile));
    try (InputStream stream = Files.newInputStream(path)) {
      // Parsed as a stream, like org.testng.xml.internal.Parser does, so that suite files relying
      // on an external entity resolve it against the working directory rather than their own.
      return new SuiteXmlParser().parse(suiteFile, stream, false);
    }
  }

  private static XmlSuite parseString(String suiteFile, String xml) {
    byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
    return new SuiteXmlParser().parse(suiteFile, new ByteArrayInputStream(bytes), false);
  }
}
