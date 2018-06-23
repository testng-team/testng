package org.testng.xml;

/** Represents the capabilities of a XML serialiser (As string) */
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
