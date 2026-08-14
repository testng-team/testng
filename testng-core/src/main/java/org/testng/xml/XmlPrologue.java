package org.testng.xml;

/**
 * Tells whether a suite file declares a doctype, by looking at what precedes its root element.
 *
 * <p>The parser has to be configured before it is built -- JAXP rejects {@code setValidating(true)}
 * together with {@code setSchema(...)} -- while a doctype is only observed once parsing is under
 * way. So the choice of grammar is made here, on the bytes, before a parser exists.
 *
 * <p>Only the prologue is scanned. XML allows nothing there but the declaration, comments,
 * processing instructions and the doctype, so reaching a start tag is proof that there is none. A
 * plain text search would be wrong: {@code <!DOCTYPE} inside a comment is not a declaration, and
 * suite files do carry commented-out ones.
 *
 * <p>Scanning bytes rather than decoded text is safe for the same reason: everything that can
 * appear before the root element is ASCII in every encoding an {@code encoding=} declaration can
 * name, save the UTF-16 family, which is handled by skipping the null bytes of its code units.
 */
final class XmlPrologue {

  private XmlPrologue() {}

  private static final byte[] DOCTYPE =
      "<!DOCTYPE".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
  private static final byte[] COMMENT_START =
      "<!--".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
  private static final byte[] COMMENT_END =
      "-->".getBytes(java.nio.charset.StandardCharsets.US_ASCII);

  /**
   * Whether a {@code <!DOCTYPE} declaration appears before the root element.
   *
   * <p>Malformed input is reported as "no doctype" rather than by throwing: this runs before the
   * parser, and a document that cannot be scanned is one the parser is about to reject with a far
   * better message than anything available here.
   */
  static boolean declaresDoctype(byte[] document) {
    int position = 0;
    while (position < document.length) {
      position = skipToMarkup(document, position);
      if (position >= document.length) {
        return false;
      }
      if (startsWith(document, position, COMMENT_START)) {
        position = skipPast(document, position + COMMENT_START.length, COMMENT_END);
      } else if (startsWith(document, position, DOCTYPE)) {
        return true;
      } else if (isProcessingInstruction(document, position)) {
        // Covers <?xml ... ?> as well: both end at the first '>' that follows a '?'.
        position = skipPastProcessingInstruction(document, position);
      } else {
        // A start tag, so the prologue is over and it held no doctype.
        return false;
      }
    }
    return false;
  }

  /**
   * Advances to the next {@code '<'}, ignoring the whitespace and encoding padding between items.
   */
  private static int skipToMarkup(byte[] document, int from) {
    int position = from;
    while (position < document.length && document[position] != '<') {
      position++;
    }
    return position;
  }

  private static boolean isProcessingInstruction(byte[] document, int position) {
    return nextByte(document, position + 1) == '?';
  }

  private static int skipPastProcessingInstruction(byte[] document, int position) {
    int cursor = position + 1;
    while (cursor < document.length) {
      if (document[cursor] == '>' && previousMeaningfulByte(document, cursor) == '?') {
        return cursor + 1;
      }
      cursor++;
    }
    return document.length;
  }

  private static int skipPast(byte[] document, int from, byte[] terminator) {
    for (int position = from; position + terminator.length <= document.length; position++) {
      if (startsWith(document, position, terminator)) {
        return position + terminator.length;
      }
    }
    return document.length;
  }

  /**
   * Compares ignoring the null padding of UTF-16, where each ASCII character is one code unit whose
   * other byte is zero. Comparing the significant bytes only makes the scan encoding-independent
   * without having to detect the encoding first.
   */
  private static boolean startsWith(byte[] document, int position, byte[] expected) {
    int cursor = position;
    for (byte b : expected) {
      cursor = skipPadding(document, cursor);
      if (cursor >= document.length || document[cursor] != b) {
        return false;
      }
      cursor++;
    }
    return true;
  }

  private static int skipPadding(byte[] document, int position) {
    int cursor = position;
    while (cursor < document.length && document[cursor] == 0) {
      cursor++;
    }
    return cursor;
  }

  private static byte nextByte(byte[] document, int position) {
    int cursor = skipPadding(document, position);
    return cursor < document.length ? document[cursor] : 0;
  }

  private static byte previousMeaningfulByte(byte[] document, int position) {
    int cursor = position - 1;
    while (cursor >= 0 && document[cursor] == 0) {
      cursor--;
    }
    return cursor >= 0 ? document[cursor] : 0;
  }
}
