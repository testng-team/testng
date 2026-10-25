package org.testng.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.net.URL;
import java.util.Objects;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.parsers.SAXParserFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * GITHUB-3324. The XML catalog and the DTD it maps to are packaged in the testng.jar that is
 * published, and a catalog resolver reads both from there.
 */
public class XmlCatalogInShadedJarTest {

  private static final String CATALOG = "org/xmlresolver/catalog.xml";

  @Test(description = "GITHUB-3324")
  public void aSuiteValidatesWithTheCatalogInTheShadedJar() throws Exception {
    String jar = "jar:" + TestNG.class.getProtectionDomain().getCodeSource().getLocation() + "!/";
    assertThat(jar).as("TestNG is loaded from the shaded jar").endsWith(".jar!/");
    URL catalog =
        Objects.requireNonNull(
            TestNG.class.getClassLoader().getResource(CATALOG),
            "the shaded jar contains " + CATALOG);
    assertThat(catalog.toString()).isEqualTo(jar + CATALOG);

    CatalogResolver resolver =
        CatalogManager.catalogResolver(
            CatalogFeatures.builder().with(CatalogFeatures.Feature.RESOLVE, "strict").build(),
            catalog.toURI());
    InputSource dtd = resolver.resolveEntity(null, "https://testng.org/testng-1.0.dtd");
    assertThat(dtd.getSystemId()).isEqualTo(jar + "testng-1.1.dtd");

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    XMLReader reader = factory.newSAXParser().getXMLReader();
    reader.setEntityResolver(resolver);
    reader.setErrorHandler(
        new DefaultHandler() {
          @Override
          public void error(SAXParseException e) throws SAXParseException {
            throw e;
          }
        });
    reader.parse(
        new InputSource(
            new StringReader(
                "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">"
                    + "<suite name=\"s\"><test name=\"t\"><classes><class name=\"a.B\"/>"
                    + "</classes></test></suite>")));
  }
}
