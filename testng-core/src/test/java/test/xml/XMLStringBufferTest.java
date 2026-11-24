package test.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.reporters.XMLStringBuffer.EOL;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Properties;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Buffer;
import org.testng.reporters.FileStringBuffer;
import org.testng.reporters.IBuffer;
import org.testng.reporters.XMLStringBuffer;

public class XMLStringBufferTest {

  @Test
  public void testMethod() {
    IBuffer result = Buffer.create();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");

    sb.push("family");
    Properties p = new Properties();
    p.setProperty("prop1", "value1");
    p.setProperty("prop2", "value2");
    sb.addRequired("cedric", "true", p);
    sb.addRequired("alois", "true");
    sb.addOptional("anne-marie", (String) null);
    sb.pop();
    String expected =
        "<family>"
            + EOL
            + "  <cedric prop2=\"value2\" prop1=\"value1\">true</cedric>"
            + EOL
            + "  <alois>true</alois>"
            + EOL
            + "</family>";
    assertThat(result.toString().trim()).isEqualTo(expected);
  }

  @Test(description = "GITHUB-1259, GITHUB-2334")
  public void addBufferAppendsWhatAddingItsXmlWouldHave() {
    // Past the size at which the buffer spills to a temporary file, which is the case addBuffer
    // exists for: toXML() would read the whole of it back as a String, and copy it once more.
    XMLStringBuffer content = new XMLStringBuffer("  ");
    for (int i = 0; i < 4000; i++) {
      content.addRequired("item", "value-" + i + " \u0007 \uFFFE " + SUPPLEMENTARY);
    }

    XMLStringBuffer streamed = new XMLStringBuffer("");
    streamed.push("root");
    streamed.addBuffer(content);
    streamed.pop("root");

    // Read back once: each call re-reads the whole temporary file and copies it, which is the very
    // thing under test.
    String materializedContent = content.toXML();

    XMLStringBuffer materialized = new XMLStringBuffer("");
    materialized.push("root");
    materialized.addString(materializedContent);
    materialized.pop("root");

    // Read raw, not through toXML(): that is the filter under test, and running it over both
    // sides let a writer that passed an illegal character through still match -- while the report
    // itself is written by toWriter, which does not filter.
    assertThat(streamed.getStringBuffer().toString()).isEqualTo(materialized.toXML());
    // And the content really did cross the threshold, so this was the streaming path.
    assertThat(materializedContent.length()).isGreaterThan(100_000);
  }

  @DataProvider
  public Object[][] inMemoryPanelSizes() {
    return new Object[][] {{0}, {5_000}, {90_000}};
  }

  @DataProvider
  public Object[][] emptyPanelVariants() {
    return new Object[][] {{false}, {true}};
  }

  @Test(dataProvider = "emptyPanelVariants", description = "GITHUB-3508")
  public void addBufferDoesNotAppendAnEmptyFilteredPanel(boolean onlyForbiddenCharacters) {
    String content = onlyForbiddenCharacters ? "\u0001\uFFFE" : "";
    FileStringBuffer actual = new FileStringBuffer(5);
    actual.append("123456");
    IBuffer target = mock(IBuffer.class, delegatesTo(actual));
    XMLStringBuffer source = new XMLStringBuffer("");
    source.addString(content);

    new XMLStringBuffer(target, "").addBuffer(source);

    verify(target, never()).append(any(CharSequence.class));
    assertThat(actual.toString()).isEqualTo("123456");
  }

  @Test(dataProvider = "inMemoryPanelSizes", description = "GITHUB-3508")
  public void addBufferDoesNotStreamAnInMemoryPanel(int size) {
    FileStringBuffer buffer = mock(FileStringBuffer.class, delegatesTo(new FileStringBuffer()));
    XMLStringBuffer content = new XMLStringBuffer(buffer, "");
    String payload = "x".repeat(size);
    content.push("open");
    content.addString(payload + "\u0001\uFFFE\uD800bad\uDC00\t\n\r" + SUPPLEMENTARY);
    String original = buffer.toString();

    XMLStringBuffer target = new XMLStringBuffer("");
    target.addString("\u0002before|");
    target.addBuffer(content);
    target.addString("|after\u0003");

    verify(buffer, never()).toWriter(any(Writer.class));
    assertThat(target.getStringBuffer().toString())
        .isEqualTo(
            "\u0002before|<open>" + EOL + payload + "bad\t\n\r" + SUPPLEMENTARY + "|after\u0003");
    assertThat(buffer.toString()).isEqualTo(original);
    content.pop("open");
    assertThat(buffer.toString()).endsWith("</open>" + EOL);
  }

  @Test(description = "GITHUB-3508")
  public void addBufferKeepsStreamingCustomBuffers() {
    IBuffer source =
        new IBuffer() {
          @Override
          public IBuffer append(CharSequence value) {
            throw new UnsupportedOperationException("Not used by this source buffer");
          }

          @Override
          public void toWriter(Writer writer) {
            try {
              writer.write("custom\u0001" + SUPPLEMENTARY);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public String toString() {
            throw new UnsupportedOperationException("This buffer must be streamed");
          }
        };
    XMLStringBuffer target = new XMLStringBuffer("");

    target.addBuffer(new XMLStringBuffer(source, ""));

    assertThat(target.getStringBuffer().toString()).isEqualTo("custom" + SUPPLEMENTARY);
  }

  @Test(description = "GITHUB-3508")
  public void addBufferKeepsStreamingAFileBufferSubclass() {
    FileStringBuffer source =
        new FileStringBuffer() {
          @Override
          public String toString() {
            throw new UnsupportedOperationException("This buffer must be streamed");
          }
        };
    source.append("content\u0001" + SUPPLEMENTARY);
    XMLStringBuffer target = new XMLStringBuffer("");

    target.addBuffer(new XMLStringBuffer(source, ""));

    assertThat(target.getStringBuffer().toString()).isEqualTo("content" + SUPPLEMENTARY);
  }

  @Test(description = "GITHUB-3508")
  public void addBufferKeepsUsingAFileBufferWriterOverride() {
    FileStringBuffer source =
        new FileStringBuffer() {
          @Override
          public void toWriter(Writer writer) {
            try {
              writer.write("custom\u0001" + SUPPLEMENTARY);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }
        };
    source.append("unused");
    XMLStringBuffer target = new XMLStringBuffer("");

    target.addBuffer(new XMLStringBuffer(source, ""));

    assertThat(target.getStringBuffer().toString()).isEqualTo("custom" + SUPPLEMENTARY);
  }

  @Test(description = "GITHUB-3508")
  public void addBufferDoesNotCallXmlStringBufferOverrides() {
    XMLStringBuffer source =
        new XMLStringBuffer("") {
          @Override
          public String toXML() {
            throw new UnsupportedOperationException("Not used when appending the backing buffer");
          }
        };
    XMLStringBuffer target =
        new XMLStringBuffer("") {
          @Override
          public void addString(String value) {
            throw new UnsupportedOperationException("Not used when appending the backing buffer");
          }
        };
    source.getStringBuffer().append("content\u0001");

    target.addBuffer(source);

    assertThat(target.getStringBuffer().toString()).isEqualTo("content");
  }

  @Test
  public void addCDATAElementWritesTheTagAndCdataOnOneLine() {
    IBuffer result = Buffer.create();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");

    sb.addCDATAElement("value", "Some Value");

    assertThat(result.toString()).isEqualTo("<value><![CDATA[Some Value]]></value>" + EOL);
  }

  @Test
  public void addCDATAElementKeepsLeadingAndTrailingWhitespace() {
    IBuffer result = Buffer.create();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");

    sb.addCDATAElement("value", "  padded  ");

    assertThat(result.toString()).isEqualTo("<value><![CDATA[  padded  ]]></value>" + EOL);
  }

  @Test
  public void addCDATASplitsTheTerminatorSequence() {
    IBuffer result = Buffer.create();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");

    sb.addCDATA("]]>");

    assertThat(result.toString()).isEqualTo("<![CDATA[]]]]><![CDATA[>]]>" + EOL);
  }

  @Test
  public void addCDATAElementSplitsTheTerminatorSequenceInsideTheTag() {
    IBuffer result = Buffer.create();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");

    sb.addCDATAElement("value", "foo]]>bar");

    assertThat(result.toString())
        .isEqualTo("<value><![CDATA[foo]]]]><![CDATA[>bar]]></value>" + EOL);
  }

  @Test
  public void addCDATAWritesTheWordNullForANull() {
    IBuffer result = Buffer.create();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");

    sb.addCDATA(null);

    assertThat(result.toString()).isEqualTo("<![CDATA[null]]>" + EOL);
  }

  @Test
  public void addCDATAStripsControlCharactersExceptLineBreaks() {
    IBuffer result = Buffer.create();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");

    sb.addCDATA("a\u0001b\nc");

    assertThat(result.toString()).isEqualTo("<![CDATA[ab\nc]]>" + EOL);
  }

  /** U+1F600, so a surrogate pair, which is the one thing the two paths could read differently. */
  private static final String SUPPLEMENTARY = new String(Character.toChars(0x1F600));
}
