package org.testng.xml;

/**
 * Represents the capabilities of a XML serializer (As string).
 *
 * <p>TestNG picks the implementation from the {@code testng.xml.weaver} system property, which
 * defaults to {@link DefaultXmlWeaver}; {@link CommentDisabledXmlWeaver} is the other one shipped.
 * A custom implementation is selected by fully qualified name and needs a public no-argument
 * constructor:
 *
 * <pre>{@code -Dtestng.xml.weaver=fully.qualified.MyWeaver}</pre>
 *
 * <p>Implementing this interface directly means writing every element of {@code testng.xml} by
 * hand. Unless that is the point, extend {@link DefaultXmlWeaver} instead and override only the
 * elements you want to change.
 */
public interface IWeaveXml {
  /**
   * Helps represent the contents of {@link XmlSuite} as a String.
   *
   * @param xmlSuite - The {@link XmlSuite} that needs to be transformed to a String.
   * @return - The String representation
   */
  String asXml(XmlSuite xmlSuite);

  /**
   * Helps represent the contents of {@link XmlTest} as a String.
   *
   * @param xmlTest - The {@link XmlTest} that needs to be transformed to a String.
   * @param indent - The indentation.
   * @return - The String representation
   */
  String asXml(XmlTest xmlTest, String indent);
}
