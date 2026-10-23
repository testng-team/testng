package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import org.testng.annotations.Test;
import org.testng.xml.internal.Parser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * GITHUB-3324. The XML catalog shipped for tools that read a {@code testng.xml} outside TestNG maps
 * every system id TestNG accepts onto the DTD TestNG resolves it to, and only onto resources that
 * exist.
 */
public class XmlCatalogTest {

  static final String CATALOG = "org/xmlresolver/catalog.xml";

  private static final String[] SCHEMES = {"https", "http"};
  private static final String[] PREFIXES = {"testng.org/", "beust.com/testng/"};
  private static final String[] DTD_NAMES = {"testng-1.0.dtd", "testng-1.1.dtd"};

  @Test(description = "GITHUB-3324")
  public void everyEntryPointsAtAResourceOnTheClasspath() throws Exception {
    URL catalog = catalogUrl();
    for (Map.Entry<String, String> entry : systemEntries().entrySet()) {
      URL target = URI.create(catalog.toString()).resolve(entry.getValue()).toURL();
      try (InputStream stream = target.openStream()) {
        assertThat(stream.readAllBytes())
            .as("the resource %s maps to, %s", entry.getKey(), target)
            .isNotEmpty();
      }
    }
  }

  @Test(description = "GITHUB-3324")
  public void theCatalogMapsEverySystemIdTestNgAccepts() throws Exception {
    List<String> expected = new ArrayList<>();
    for (String scheme : SCHEMES) {
      for (String prefix : PREFIXES) {
        for (String name : DTD_NAMES) {
          String systemId = scheme + "://" + prefix + name;
          assertThat(TestNGURLs.isDTDDomainInternallyKnownToTestNG(systemId))
              .as("TestNG resolves %s from the classpath", systemId)
              .isTrue();
          expected.add(systemId);
        }
      }
    }
    assertThat(systemEntries().keySet()).containsExactlyInAnyOrderElementsOf(expected);
  }

  @Test(description = "GITHUB-3324")
  public void everyEntryPointsAtTheDtdTestNgResolvesTo() throws Exception {
    URL catalog = catalogUrl();
    URL dtd = Objects.requireNonNull(getClass().getClassLoader().getResource(Parser.TESTNG_DTD));
    for (Map.Entry<String, String> entry : systemEntries().entrySet()) {
      assertThat(URI.create(catalog.toString()).resolve(entry.getValue()).toString())
          .as("the resource %s maps to", entry.getKey())
          .isEqualTo(dtd.toString());
    }
  }

  @Test(description = "GITHUB-3324")
  public void theJdkCatalogResolverValidatesASuiteWithoutTheNetwork() throws Exception {
    CatalogResolver resolver = strictResolver();
    String suite =
        "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">"
            + "<suite name=\"s\"><test name=\"t\"><classes><class name=\"a.B\"/></classes>"
            + "</test></suite>";

    List<SAXParseException> errors = validate(suite, resolver);

    assertThat(errors).isEmpty();
  }

  @Test(description = "GITHUB-3324")
  public void theDtdFromTheCatalogStillRejectsAnInvalidSuite() throws Exception {
    String suite =
        "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">"
            + "<suite name=\"s\"><unknown/></suite>";

    List<SAXParseException> errors = validate(suite, strictResolver());

    assertThat(errors).isNotEmpty();
  }

  @Test(description = "GITHUB-3324")
  public void aStrictResolverRefusesAnIdTheCatalogDoesNotMap() throws Exception {
    CatalogResolver resolver = strictResolver();
    assertThatThrownBy(() -> resolver.resolveEntity(null, "https://example.com/other.dtd"))
        .isInstanceOf(CatalogException.class);
  }

  /** A resolver that fails on an unmapped id instead of going to the network. */
  private static CatalogResolver strictResolver() throws Exception {
    CatalogFeatures features =
        CatalogFeatures.builder().with(CatalogFeatures.Feature.RESOLVE, "strict").build();
    return CatalogManager.catalogResolver(features, catalogUrl().toURI());
  }

  private static List<SAXParseException> validate(String suite, CatalogResolver resolver)
      throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    factory.setNamespaceAware(false);
    XMLReader reader = factory.newSAXParser().getXMLReader();
    reader.setEntityResolver(resolver);
    reader.setContentHandler(new DefaultHandler());
    List<SAXParseException> errors = new ArrayList<>();
    reader.setErrorHandler(
        new ErrorHandler() {
          @Override
          public void warning(SAXParseException e) {}

          @Override
          public void error(SAXParseException e) {
            errors.add(e);
          }

          @Override
          public void fatalError(SAXParseException e) throws SAXParseException {
            throw e;
          }
        });
    reader.parse(new InputSource(new StringReader(suite)));
    return errors;
  }

  private static URL catalogUrl() {
    return Objects.requireNonNull(
        XmlCatalogTest.class.getClassLoader().getResource(CATALOG),
        CATALOG + " is on the classpath");
  }

  /** The {@code systemId} to {@code uri} pairs of the catalog, in document order. */
  private static Map<String, String> systemEntries() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Map<String, String> entries = new LinkedHashMap<>();
    try (InputStream stream = catalogUrl().openStream()) {
      NodeList systems =
          factory
              .newDocumentBuilder()
              .parse(stream)
              .getElementsByTagNameNS("urn:oasis:names:tc:entity:xmlns:xml:catalog", "system");
      for (int i = 0; i < systems.getLength(); i++) {
        Element system = (Element) systems.item(i);
        entries.put(system.getAttribute("systemId"), system.getAttribute("uri"));
      }
    }
    assertThat(entries).as("the system entries of %s", CATALOG).isNotEmpty();
    return entries;
  }
}
