package org.testng.xml;

import org.testng.TestNGException;

import java.io.InputStream;

public interface IFileParser<T> {

  T parse(String filePath, InputStream is, boolean loadClasses) throws TestNGException;

}
