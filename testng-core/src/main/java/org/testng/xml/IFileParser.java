package org.testng.xml;

import java.io.InputStream;
import org.testng.TestNGException;

public interface IFileParser<T> {

  T parse(String filePath, InputStream is, boolean loadClasses) throws TestNGException;
}
