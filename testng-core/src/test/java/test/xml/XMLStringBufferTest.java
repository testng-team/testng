package test.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.reporters.XMLStringBuffer.EOL;

import java.util.Properties;
import org.testng.annotations.Test;
import org.testng.reporters.Buffer;
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

    assertThat(streamed.toXML()).isEqualTo(materialized.toXML());
    // And the content really did cross the threshold, so this was the streaming path.
    assertThat(materializedContent.length()).isGreaterThan(100_000);
  }

  /** U+1F600, so a surrogate pair, which is the one thing the two paths could read differently. */
  private static final String SUPPLEMENTARY = new String(Character.toChars(0x1F600));
}
