package org.testng.xml;

public interface ISuiteParser extends IFileParser<XmlSuite> {

  boolean accept(String fileName);
}
