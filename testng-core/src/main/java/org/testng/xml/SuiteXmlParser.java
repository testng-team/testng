package org.testng.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.testng.TestNGException;
import org.testng.xml.internal.Parser;
import org.xml.sax.SAXException;

public class SuiteXmlParser extends XMLParser<XmlSuite> implements ISuiteParser {

  @Override
  public XmlSuite parse(
      String currentFile, @Nullable InputStream inputStream, boolean loadClasses) {
    TestNGContentHandler contentHandler = new TestNGContentHandler(currentFile, loadClasses);

    try {
      // Nullable on the interface for the parsers that read a source of their own. This one is
      // reached either through accept(), which requires a file: scheme and so a stream, or as
      // Parser's fallback for a scheme nothing claims -- which has never been readable here.
      parse(
          Objects.requireNonNull(inputStream, "a file: suite is read from an open stream"),
          contentHandler);

      return Objects.requireNonNull(
          contentHandler.getSuite(), "the document declares no <suite> element");
    } catch (SAXException | IOException e) {
      throw new TestNGException(e);
    }
  }

  /** This is the parser that reads suites, so this is the one the suite schema applies to. */
  @Override
  protected boolean validatesAgainstSuiteSchema() {
    return true;
  }

  @Override
  public boolean accept(String fileName) {
    return Parser.hasFileScheme(fileName) && fileName.endsWith(".xml");
  }
}
