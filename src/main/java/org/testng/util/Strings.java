package org.testng.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Strings {
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.length() == 0; // string.isEmpty() in Java 6
  }

  private static final List<String> ESCAPE_HTML_LIST = Arrays.asList(
    "&", "&amp;",
    "<", "&lt;",
    ">", "&gt;"
  );
  
  private static final Map<String, String> ESCAPE_HTML_MAP = new LinkedHashMap<>();

  static {
    for (int i = 0; i < ESCAPE_HTML_LIST.size(); i += 2) {
      ESCAPE_HTML_MAP.put(ESCAPE_HTML_LIST.get(i), ESCAPE_HTML_LIST.get(i + 1));
    }
  }

  public static String escapeHtml(String text) {
    String result = text;
    for (Map.Entry<String, String> entry : ESCAPE_HTML_MAP.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public static void main(String[] args) {
    System.out.println(escapeHtml("10 < 20 && 30 > 20"));
  }
}
