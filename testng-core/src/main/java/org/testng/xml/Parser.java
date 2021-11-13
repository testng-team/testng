package org.testng.xml;

import java.io.InputStream;

/**
 * <code>Parser</code> is a parser for a TestNG XML test suite file.
 *
 * @deprecated - This class stands deprecated as of TestNG <code>7.5.0</code>. There are no
 *     alternatives for this class.
 */
@SuppressWarnings("unused")
@Deprecated
public class Parser extends org.testng.xml.internal.Parser {

  public Parser(String path) {
    super(path);
  }

  public Parser(InputStream is) {
    super(is);
  }
}
