package org.testng.util;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.List;
import java.util.Map;

public class Strings {
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.length() == 0; // string.isEmpty() in Java 6
  }

  private static List<String> ESCAPE_HTML_LIST = Lists.newArrayList(
    "&", "&amp;",
    "<", "&lt;",
    ">", "&gt;"
  );
  
  private static final Map<String, String> ESCAPE_HTML_MAP = Maps.newLinkedHashMap();

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
