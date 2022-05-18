package org.testng.util;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.testng.collections.Maps;

public final class Strings {
  private Strings() {
    // Utility class. Defeat instantiation.
  }

  public static boolean isNullOrEmpty(String string) {
    return Optional.ofNullable(string).orElse("").trim().isEmpty();
  }

  public static boolean isNotNullAndNotEmpty(String string) {
    return !isNullOrEmpty(string);
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

  // Don't remove this method. This is being called as part of the Gradle build. Removing this
  // causes build to fail due to NoSuchMethodError
  public static String join(String delimiter, String[] parts) {
    return String.join(delimiter, parts);
  }
}
