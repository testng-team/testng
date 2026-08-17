package org.testng.xml;

import java.io.InputStream;
import org.jspecify.annotations.Nullable;
import org.testng.TestNGException;

public interface IFileParser<T> {

  /**
   * @param is the file's contents, or {@code null} for a parser that reads a source of its own --
   *     {@code Parser} only opens a stream for a {@code file:} scheme.
   */
  T parse(String filePath, @Nullable InputStream is, boolean loadClasses) throws TestNGException;
}
