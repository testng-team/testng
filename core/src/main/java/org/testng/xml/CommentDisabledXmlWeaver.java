package org.testng.xml;

/**
 * This class provides String representation of both {@link XmlSuite} and {@link XmlTest} without
 * adding an XML comment as the test name and suite name at the end of the corresponding tags.
 */
final class CommentDisabledXmlWeaver extends DefaultXmlWeaver {
  CommentDisabledXmlWeaver() {
    super("");
  }
}
