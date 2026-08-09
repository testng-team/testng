package org.testng.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.xml.sax.InputSource;

public final class TestNGContentHandlerTestSupport {
  private TestNGContentHandlerTestSupport() {}

  public static void configureConnection(URLConnection connection) {
    TestNGContentHandler.configureConnection(connection);
  }

  public static byte[] readUrl(URL url) throws IOException {
    return TestNGContentHandler.readUrl(url, true);
  }

  public static InputSource readUrlAsInputSource(URL url) throws IOException {
    return new InputSource(new ByteArrayInputStream(TestNGContentHandler.readUrl(url, true)));
  }
}
