package org.testng.util;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.testng.collections.Maps;

public final class Strings {
  private Strings() {
    // Utility class. Defeat instantiation.
  }

  // TODO: When TestNG moves to JDK11 as the default JDK this method needs to be deprecated and
  // removed
  // because now this method is present in JDK11 as part of the JDK itself.
  // See
  // https://hg.openjdk.java.net/jdk/jdk/file/fc16b5f193c7/src/java.base/share/classes/java/lang/String.java#l2984
  /** @deprecated - This method stands deprecated as of TestNG <code>7.6.0</code> */
  @Deprecated
  public static String repeat(String text, int count) {
    return text.repeat(count);
  }

  public static boolean isNullOrEmpty(String string) {
    return Optional.ofNullable(string).orElse("").trim().isEmpty();
  }

  public static boolean isNotNullAndNotEmpty(String string) {
    return !isNullOrEmpty(string);
  }

  /**
   * @param string - The input String.
   * @return - Returns an empty string if the input String is <code>null</code> (or) empty, else it
   *     returns back the input string.
   * @deprecated - This method stands deprecated as of TestNG <code>7.6.0</code>
   */
  @Deprecated
  public static String getValueOrEmpty(String string) {
    return Optional.ofNullable(string).orElse("");
  }

  private static final Map<String, String> ESCAPE_HTML_MAP = Maps.newLinkedHashMap();

  static {
    ESCAPE_HTML_MAP.put("&", "&amp;");
    ESCAPE_HTML_MAP.put("<", "&lt;");
    ESCAPE_HTML_MAP.put(">", "&gt;");
  }

  public static String escapeHtml(String text) {
    String result = text;
    for (Map.Entry<String, String> entry : ESCAPE_HTML_MAP.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public static String valueOf(Map<?, ?> m) {
    return m.values().stream().map(Object::toString).collect(Collectors.joining(" "));
  }

  /** @deprecated - This is deprecated of TestNG <code>7.6.0</code> */
  @Deprecated
  public static String join(String delimiter, String[] parts) {
    return String.join(delimiter, parts);
  }
}
