package org.testng.xml;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Tells whether a suite file declares a doctype, by looking at what precedes its root element.
 *
 * <p>The parser has to be configured before it is built -- JAXP rejects {@code setValidating(true)}
 * together with {@code setSchema(...)} -- while a doctype is only observed once parsing is under
 * way. So the choice of grammar is made here, before a parser exists.
 *
 * <p>Read with a real XML parser rather than by searching the bytes. {@code <!DOCTYPE} inside a
 * comment is not a declaration, and suite files do carry commented-out ones, so a text search gets
 * it wrong; a hand-written scanner then has to know about comments, processing instructions and
 * every encoding a document can be written in. Stopping at the first {@code DTD} or {@code
 * START_ELEMENT} event costs one pass over a prologue.
 */
final class XmlPrologue {

  private XmlPrologue() {}

  /**
   * Whether a {@code <!DOCTYPE} declaration appears before the root element.
   *
   * <p>Malformed input is reported as "no doctype" rather than by throwing: this runs before the
   * parse, and a document that cannot be read here is one the parser is about to reject with a
   * message that names the line and column.
   */
  static boolean declaresDoctype(byte[] document) {
    XMLStreamReader reader = null;
    try {
      reader = newFactory().createXMLStreamReader(new ByteArrayInputStream(document));
      while (reader.hasNext()) {
        int event = reader.next();
        if (event == XMLStreamConstants.DTD) {
          return true;
        }
        if (event == XMLStreamConstants.START_ELEMENT) {
          return false;
        }
      }
      return false;
    } catch (XMLStreamException e) {
      return false;
    } finally {
      closeQuietly(reader);
    }
  }

  /**
   * A factory per call, as {@code XMLParser} builds a SAX parser per parse: implementations are not
   * required to be thread-safe, and suite files are read concurrently.
   *
   * <p>{@code SUPPORT_DTD} has to stay on, since turning it off is what would hide the very event
   * this looks for. External entities are not resolved: the declaration is all that matters here,
   * and reaching out to whatever a suite file names -- before the parse that has a resolver for it
   * -- would be both slow and unsafe.
   */
  private static XMLInputFactory newFactory() {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    return factory;
  }

  private static void closeQuietly(XMLStreamReader reader) {
    if (reader == null) {
      return;
    }
    try {
      reader.close();
    } catch (XMLStreamException e) {
      // Closing a reader over a byte array releases nothing that matters, and letting this escape
      // would replace whatever the caller was about to report.
    }
  }
}
