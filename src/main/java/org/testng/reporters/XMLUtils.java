package org.testng.reporters;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Static helpers for XML.
 *
 * @author Cedric Beust Jul 21, 2003
 *
 */
public final class XMLUtils {

  /** Platform specific end of line */
  private static final String EOL = System.getProperty("line.separator");

  private XMLUtils() {
    // Hide constructor
  }

  static public String xml(String indent, String elementName, String content,
      Properties attributes) {
    StringBuffer result = new StringBuffer();
    xmlOpen(result, indent, elementName, attributes, true /* no newline */);
    result.append(content);
    xmlClose(result, "", elementName, XMLUtils.extractComment(elementName, attributes));

    return result.toString();
  }

  public static String extractComment(String tag, Properties properties) {
    if (properties == null || "span".equals(tag)) return null;

    String[] attributes = new String[] { "id", "name", "class" };
    for (String a : attributes) {
      String comment = properties.getProperty(a);
      if (comment != null) {
        return " <!-- " + comment.replaceAll("[-]{2,}", "-") + " -->";
      }
    }

    return null;
  }

  public static void xmlOptional(StringBuffer result, String sp,
      String elementName, Boolean value, Properties attributes) {
    if (null != value) {
      xmlRequired(result, sp, elementName, value.toString(), attributes);
    }
  }

  public static void xmlOptional(StringBuffer result, String sp,
      String elementName, String value, Properties attributes) {
    if (null != value) {
      xmlRequired(result, sp, elementName, value, attributes);
    }
  }

  public static void xmlRequired(StringBuffer result, String sp,
      String elementName, String value, Properties attributes) {
    result.append(xml(sp, elementName, value, attributes));
  }

  public static void xmlOpen(StringBuffer result, String indent, String tag,
      Properties attributes) {
    xmlOpen(result, indent, tag, attributes, false /* no newline */);
  }

  /**
   * Appends the attributes to result. The attributes are added on a single line
   * as: key1="value1" key2="value2" ... (a space is added before the first key)
   *
   * @param result
   *          the buffer to append attributes to.
   * @param attributes
   *          the attributes to append (may be null).
   */
  public static void appendAttributes(StringBuffer result, Properties attributes) {
    if (null != attributes) {
      for (Object element : attributes.entrySet()) {
        Entry entry = (Entry) element;
        String key = entry.getKey().toString();
        String value = escape(entry.getValue().toString());
        result.append(" ").append(key).append("=\"").append(value).append("\"");
      }
    }
  }

  public static void xmlOpen(StringBuffer result, String indent, String tag,
      Properties attributes, boolean noNewLine) {
    result.append(indent).append("<").append(tag);
    appendAttributes(result, attributes);
    result.append(">");
    if (!noNewLine) {
      result.append(EOL);
    }
  }

  public static void xmlClose(StringBuffer result, String indent, String tag, String comment) {
    result.append(indent).append("</").append(tag).append(">")
        .append(comment != null ? comment : "")
        .append(EOL);
  }

  public static String escape(String input) {
    if (input == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    StringCharacterIterator iterator = new StringCharacterIterator(input);
    char character = iterator.current();
    while (character != CharacterIterator.DONE) {
      if (character == '<') {
        result.append("&lt;");
      } else if (character == '>') {
        result.append("&gt;");
      } else if (character == '\"') {
        result.append("&quot;");
      } else if (character == '\'') {
        result.append("&#039;");
      } else if (character == '&') {
        result.append("&amp;");
      } else {
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
}
