package org.testng.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

final class TestNGURLs {

  private TestNGURLs() {}

  private static final List<String> DOMAINS = Arrays.asList("beust.com", "testng.org");

  static boolean isDTDDomainInternallyKnownToTestNG(String publicId) {
    try {
      URL url = new URL(publicId.toLowerCase().trim());
      return DOMAINS.contains(url.getHost());
    } catch (MalformedURLException e) {
      return false;
    }
  }
}
