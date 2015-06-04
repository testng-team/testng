package org.testng.xml;

public interface SuiteParser extends IFileParser<XmlSuite> {

  boolean accept(String fileName);
}
