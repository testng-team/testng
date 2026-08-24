package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;
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

  /**
   * The stream a suite is read from must be closed by the time the parse returns.
   *
   * <p>{@code Parser} opens a {@code FileInputStream} per suite file and never closes it, leaving
   * that to whoever consumes it; {@code JarFileUtils} then deletes the directory it extracted a jar
   * into, right after parsing. On Windows a file still held open cannot be deleted, so a stream
   * left behind here surfaces far away, as a {@code FileSystemException} in a jar test -- and on
   * Linux and macOS not at all, which is why this asserts the invariant directly rather than
   * through a deletion.
   *
   * <p>Both modes are covered because they take different paths: only the validating one reads the
   * document itself, to choose the grammar before building a parser.
   */
  @Test(dataProvider = "validationModes")
  public void theStreamIsClosedByTheTimeTheParseReturns(String mode) throws Exception {
    String previous = System.getProperty(RuntimeBehavior.XML_VALIDATION_MODE);
    System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, mode);
    ClosingAwareStream stream = new ClosingAwareStream(SUITE.getBytes(StandardCharsets.UTF_8));

    try {
      new SuiteXmlParser().parse(stream, new NameCollector());

      assertThat(stream.closed).as("the suite stream must be closed in [%s] mode", mode).isTrue();
    } finally {
      if (previous == null) {
        System.clearProperty(RuntimeBehavior.XML_VALIDATION_MODE);
      } else {
        System.setProperty(RuntimeBehavior.XML_VALIDATION_MODE, previous);
      }
    }
  }

  @DataProvider
  public static Object[][] validationModes() {
    return new Object[][] {{"warn"}, {"strict"}, {"off"}};
  }

  private static final class ClosingAwareStream extends ByteArrayInputStream {
    private boolean closed;

    ClosingAwareStream(byte[] bytes) {
      super(bytes);
    }

    @Override
    public void close() throws java.io.IOException {
      closed = true;
      super.close();
    }
  }

  private static void parse(DefaultHandler handler) throws Exception {
    byte[] bytes = SUITE.getBytes(StandardCharsets.UTF_8);
    new SuiteXmlParser().parse(new ByteArrayInputStream(bytes), handler);
  }

  private static final class NameCollector extends DefaultHandler {
    /** Null until a {@code <suite>} element carrying a name has been seen. */
    private @Nullable String suiteName;

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) {
      if ("suite".equals(name)) {
        suiteName = attributes.getValue("name");
      }
    }
  }
}
