package org.testng.util;

import org.testng.collections.Maps;
import org.testng.internal.collections.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Strings {
  private Strings() {
    //Utility class. Defeat instantiation.
  }
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.trim().isEmpty();
  }

  public static boolean isNotNullAndNotEmpty(String string) {
    return ! (isNullOrEmpty(string));
  }

  private static List<Pair<String, String>> ESCAPE_HTML_LIST = Arrays.asList(
          Pair.create("&", "&amp;"),
          Pair.create("<", "&lt;"),
          Pair.create(">", "&gt;")
  );

  private static final Map<String, String> ESCAPE_HTML_MAP = Maps.newLinkedHashMap();

  static {
    for (Pair<String,String> each : ESCAPE_HTML_LIST) {
      ESCAPE_HTML_MAP.put(each.first(), each.second());
    }
  }

  public static String escapeHtml(String text) {
    String result = text;
    for (Map.Entry<String, String> entry : ESCAPE_HTML_MAP.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }

}
