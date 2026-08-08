package org.testng.xml;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public final class TestNGContentHandlerTestSupport {
  private TestNGContentHandlerTestSupport() {}

  public static void configureConnection(URLConnection connection) {
    TestNGContentHandler.configureConnection(connection);
  }

  public static byte[] readUrl(URL url) throws IOException {
    return TestNGContentHandler.readUrl(url, true);
  }
}
