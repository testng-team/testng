package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * That folding one buffer into another drops exactly the characters {@link XMLStringBuffer#toXML()}
 * drops -- {@code toXML()} is the oracle rather than a copy of its regular expression, since
 * agreeing with it is the whole contract.
 *
 * <p>The chunk sizes below straddle {@link XmlCharFilteringWriter#SLICE}, which is where a
 * surrogate pair can fall across two {@code write} calls; {@code XmlCharFilteringWriter} says what
 * that costs.
 */
public class XmlCharFilteringWriterTest {

  /** U+1F600, a character of the supplementary planes, so a surrogate pair. */
  private static final String SUPPLEMENTARY = new String(Character.toChars(0x1F600));

  /** Larger than anything here, so the whole content arrives in one write. */
  private static final int WHOLE = 1 << 20;

  @DataProvider(name = "contents")
  public Object[][] contents() {
    return new Object[][] {
      {"empty", ""},
      {"plain", "<a href=\"#\">text</a>"},
      {"the whitespace XML allows", "a\tb\nc\rd"},
      {"a C0 control XML does not allow", "abc\u0007def"},
      {"the two non-characters of the BMP", "a\uFFFEb\uFFFFc"},
      {"a lone high surrogate", "a\uD83Db"},
      {"a lone low surrogate", "a\uDE00b"},
      {"a high surrogate at the very end", "ab\uD83D"},
      {"a supplementary character", "a" + SUPPLEMENTARY + "b"},
      {"supplementary characters back to back", SUPPLEMENTARY + SUPPLEMENTARY + SUPPLEMENTARY},
      {"a high surrogate followed by another one", "a\uD83D\uD83D" + SUPPLEMENTARY},
      {"everything at once", "a\uD83D" + SUPPLEMENTARY + "\uFFFE\uDE00\tz"},
      {"a supplementary character on a chunk boundary", onChunkBoundary()},
    };
  }

  @Test(dataProvider = "contents")
  public void whatIsWrittenIsWhatToXmlKeeps(String description, String content) {
    // Every chunking around the writer's own slice size, so a pair straddles a write() call.
    int slice = XmlCharFilteringWriter.SLICE;
    for (int chunk : new int[] {1, 2, 3, slice - 1, slice, slice + 1, WHOLE}) {
      assertThat(filtered(content, chunk))
          .as("%s, written %s characters at a time", description, chunk)
          .isEqualTo(keptByToXml(content));
    }
  }

  @Test(
      description =
          "GITHUB-1259: no slice ends between the halves of a surrogate pair, at any alignment")
  public void aSurrogatePairIsNeverSplitAcrossTwoSlices() {
    // The buffer this writes into may spill to a temporary file, and FileStringBuffer encodes each
    // append on its own -- so a slice ending on a high surrogate reaches the file as '?', and the
    // low surrogate opening the next one as a second '?'. It took a CRLF line separator to shift
    // one pair onto that boundary, which is why only the Windows CI rows saw it.
    for (int shift = 0; shift <= XmlCharFilteringWriter.SLICE + 1; shift++) {
      String content = "a".repeat(shift) + SUPPLEMENTARY + "a".repeat(XmlCharFilteringWriter.SLICE);
      RecordingBuffer recorded = new RecordingBuffer();
      XmlCharFilteringWriter writer = new XmlCharFilteringWriter(recorded);
      writer.write(content.toCharArray(), 0, content.length());
      writer.close();

      assertThat(String.join("", recorded.slices)).as("at shift %s", shift).isEqualTo(content);
      for (String slice : recorded.slices) {
        assertThat(Character.isHighSurrogate(slice.charAt(slice.length() - 1)))
            .as("slice ending on a lone high surrogate, at shift %s", shift)
            .isFalse();
      }
    }
  }

  /** A run of text with a surrogate pair straddling the writer's slice size. */
  private static String onChunkBoundary() {
    return "a".repeat(XmlCharFilteringWriter.SLICE - 1) + SUPPLEMENTARY + "tail";
  }

  /** Keeps every slice the writer hands over, which is what the invariant above is about. */
  private static final class RecordingBuffer implements IBuffer {
    private final List<String> slices = new ArrayList<>();

    @Override
    public IBuffer append(CharSequence string) {
      slices.add(string.toString());
      return this;
    }

    @Override
    public void toWriter(Writer fw) {
      throw new UnsupportedOperationException("Not what this buffer is for");
    }
  }

  private static String keptByToXml(String content) {
    XMLStringBuffer xsb = new XMLStringBuffer("");
    xsb.addString(content);
    return xsb.toXML();
  }

  private static String filtered(String content, int chunk) {
    IBuffer buffer = Buffer.create();
    XmlCharFilteringWriter writer = new XmlCharFilteringWriter(buffer);
    char[] characters = content.toCharArray();
    for (int offset = 0; offset < characters.length; offset += chunk) {
      writer.write(characters, offset, Math.min(chunk, characters.length - offset));
    }
    writer.close();
    return buffer.toString();
  }
}
