package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.annotations.Test;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParserTest {

  private static final String SUITE =
      "<suite name=\"s\"><test name=\"t\"><classes/></test></suite>";

  /**
   * A parse must not wait for another one to finish.
   *
   * <p>{@code XMLParser} used to keep one shared {@link javax.xml.parsers.SAXParser} behind a class
   * wide lock held for the whole parse. Entity resolution happens inside that call, and {@link
   * TestNGContentHandler} resolves an unknown system id over HTTP with no timeout, so a single
   * suite pointing at an unreachable DTD mirror blocked suite parsing everywhere.
   *
   * <p>The blocking is simulated in the content handler rather than through a DTD, so the test does
   * not depend on the JAXP implementation resolving an external subset, nor on the network.
   *
   * <p>Under the old code this test does not fail an assertion -- it hangs, which is the point, so
   * it carries a timeout.
   */
  @Test(timeOut = 30_000)
  public void aBlockedParseDoesNotHoldUpAnotherOne() throws Exception {
    CountDownLatch parsing = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    AtomicReference<Throwable> blockedFailure = new AtomicReference<>();

    Thread blocked =
        new Thread(
            () -> {
              try {
                parse(
                    new DefaultHandler() {
                      @Override
                      public void startElement(String u, String l, String name, Attributes a)
                          throws org.xml.sax.SAXException {
                        parsing.countDown();
                        try {
                          release.await();
                        } catch (InterruptedException e) {
                          Thread.currentThread().interrupt();
                          throw new org.xml.sax.SAXException(e);
                        }
                      }
                    });
              } catch (Throwable t) {
                blockedFailure.set(t);
              }
            },
            "blocked-parse");
    blocked.start();

    try {
      assertThat(parsing.await(10, TimeUnit.SECONDS))
          .as("the first parse should have reached the content handler")
          .isTrue();

      // Under the old code this call waits for the lock the blocked parse is holding, forever.
      NameCollector collector = new NameCollector();
      parse(collector);

      assertThat(collector.suiteName).isEqualTo("s");
    } finally {
      release.countDown();
      blocked.join(10_000);
    }

    assertThat(blockedFailure.get()).isNull();
  }

  private static void parse(DefaultHandler handler) throws Exception {
    byte[] bytes = SUITE.getBytes(StandardCharsets.UTF_8);
    new SuiteXmlParser().parse(new ByteArrayInputStream(bytes), handler);
  }

  private static final class NameCollector extends DefaultHandler {
    private String suiteName;

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) {
      if ("suite".equals(name)) {
        suiteName = attributes.getValue("name");
      }
    }
  }
}
