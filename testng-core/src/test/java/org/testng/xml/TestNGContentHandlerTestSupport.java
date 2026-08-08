package org.testng.xml;

import java.net.URLConnection;

public final class TestNGContentHandlerTestSupport {
  private TestNGContentHandlerTestSupport() {}

  public static void configureConnection(URLConnection connection) {
    TestNGContentHandler.configureConnection(connection);
  }
}
