package org.testng.xml;

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
import java.util.Objects;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.xml.internal.Parser;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * The suite files of the test corpus, and the few things every test that walks them needs.
 *
 * <p>Shared rather than duplicated because the details are easy to get subtly wrong and each one
 * was paid for: how a suite file is opened decides whether an external entity resolves, and how a
 * doctype is resolved decides whether the test touches the network.
 */
final class SuiteCorpus {

  private SuiteCorpus() {}

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
          .filter(SuiteCorpus::declaresASuite)
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

  private static Path pathOf(String suiteFile) {
    return Paths.get(getPathToResource(suiteFile));
  }

  /**
   * Opens a suite file as a bare stream, without a system id.
   *
   * <p>That is what {@link org.testng.xml.internal.Parser} does, and {@code xml/issue2501/2501.xml}
   * depends on it: its external entity is declared relative to the module directory, so it only
   * resolves when the document has no base URI of its own.
   */
  static InputStream open(String suiteFile) throws IOException {
    return Files.newInputStream(pathOf(suiteFile));
  }

  static XmlSuite parseFile(String suiteFile) throws IOException {
    try (InputStream stream = open(suiteFile)) {
      return new SuiteXmlParser().parse(suiteFile, stream, false);
    }
  }

  static XmlSuite parseString(String suiteFile, String xml) {
    byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
    return new SuiteXmlParser().parse(suiteFile, new ByteArrayInputStream(bytes), false);
  }

  /**
   * Serves the bundled DTD for any doctype, so that no test ever reaches testng.org, and defers to
   * the parser for everything else.
   *
   * <p>The {@code .dtd} test is what keeps {@code 2501.xml} working: substituting the DTD for
   * <em>every</em> entity would feed it to its {@code &params;} reference as well.
   */
  static EntityResolver bundledDtdResolver() {
    return (publicId, systemId) -> {
      if (systemId == null || !systemId.endsWith(".dtd")) {
        return null;
      }
      InputStream dtd = SuiteCorpus.class.getClassLoader().getResourceAsStream(Parser.TESTNG_DTD);
      return new InputSource(
          Objects.requireNonNull(dtd, Parser.TESTNG_DTD + " is not on the test classpath"));
    };
  }
}
