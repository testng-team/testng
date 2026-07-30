package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static test.SimpleBaseTest.getPathToResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
          .map(path -> new Object[] {root.relativize(path).toString()})
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
