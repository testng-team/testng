package test.xml;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.TestNGContentHandler;
import org.testng.xml.TestNGContentHandlerTestSupport;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.xml.sax.SAXException;
import test.SimpleBaseTest;

public class TestNGContentHandlerTest extends SimpleBaseTest {
  @Test
  public void testDescriptionInclusion() throws Exception {
    final String xml = getPathToResource("xml/simple-suite-with-method-desc.xml");
    SuiteXmlParser parser = new SuiteXmlParser();
    TestNGContentHandler handler = new TestNGContentHandler(xml, false);
    parser.parse(new FileInputStream(xml), handler);
    List<XmlInclude> includes =
        handler.getSuite().getTests().get(0).getXmlClasses().get(0).getIncludedMethods();
    String desc = includes.get(0).getDescription();
    assertThat("simple-description").isEqualTo(desc);
  }

  @Test(description = "GITHUB-2501")
  public void ensureAppropriateConnectionObjectsAreUsed() throws IOException, SAXException {
    String xml = getPathToResource("xml/issue2501/2501.xml");
    SuiteXmlParser parser = new SuiteXmlParser();
    TestNGContentHandler handler = new TestNGContentHandler(xml, false);
    parser.parse(new FileInputStream(xml), handler);
    XmlClass xmlClass = handler.getSuite().getTests().get(0).getXmlClasses().get(0);
    assertThat(xmlClass.getSupportClass()).isEqualTo(test.xml.issue2501.TestClassSample.class);
  }

  @Test(description = "GITHUB-3316")
  public void resolverConfiguresDtdConnectionTimeouts() throws Exception {
    URLConnection connection =
        new URLConnection(new URL("https://testng.org/testng-1.1.dtd")) {
          @Override
          public void connect() throws IOException {}
        };

    TestNGContentHandlerTestSupport.configureConnection(connection);

    assertThat(connection.getConnectTimeout()).isEqualTo(10_000);
    assertThat(connection.getReadTimeout()).isEqualTo(10_000);
  }

  @Test(description = "GITHUB-3316")
  public void resolverBuffersAndClosesExternalDtd() throws Exception {
    byte[] content = "<!ELEMENT suite EMPTY>".getBytes(StandardCharsets.UTF_8);
    AtomicBoolean sourceClosed = new AtomicBoolean();
    URL dtd =
        new URL(
            null,
            "memory://testng.dtd",
            new URLStreamHandler() {
              @Override
              protected URLConnection openConnection(URL url) {
                return new URLConnection(url) {
                  @Override
                  public void connect() throws IOException {}

                  @Override
                  public ByteArrayInputStream getInputStream() {
                    return new ByteArrayInputStream(content) {
                      @Override
                      public void close() throws IOException {
                        sourceClosed.set(true);
                        super.close();
                      }
                    };
                  }
                };
              }
            });

    var resolved = TestNGContentHandlerTestSupport.readUrlAsInputSource(dtd);
    assertThat(sourceClosed).isTrue();
    try (var input = resolved.getByteStream()) {
      assertThat(input.readAllBytes()).isEqualTo(content);
    }
  }

  @Test(description = "GITHUB-3316")
  public void resolverFollowsOnlyOneRedirect() throws Exception {
    AtomicInteger secondRequests = new AtomicInteger();
    AtomicInteger thirdRequests = new AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/first",
        exchange -> {
          exchange.getResponseHeaders().add("Location", "/second");
          exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
          exchange.close();
        });
    server.createContext(
        "/second",
        exchange -> {
          secondRequests.incrementAndGet();
          exchange.getResponseHeaders().add("Location", "/third");
          exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
          exchange.close();
        });
    server.createContext(
        "/third",
        exchange -> {
          thirdRequests.incrementAndGet();
          exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
          exchange.close();
        });
    server.start();

    try {
      URL dtd = new URL("http://127.0.0.1:" + server.getAddress().getPort() + "/first");
      TestNGContentHandlerTestSupport.readUrl(dtd);
      assertThat(secondRequests).hasValue(1);
      assertThat(thirdRequests).hasValue(0);
    } finally {
      server.stop(0);
    }
  }

  public static class LocalTestClass {
    @Test
    public void helloWorld() {}
  }
}
