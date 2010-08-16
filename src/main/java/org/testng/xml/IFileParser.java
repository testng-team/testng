package org.testng.xml;

import org.testng.TestNGException;

import java.io.InputStream;

public interface IFileParser {

  XmlSuite parse(String filePath, InputStream is) throws TestNGException;

}
