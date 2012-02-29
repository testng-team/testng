package org.testng.util;

public class Strings {
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.length() == 0; // string.isEmpty() in Java 6
  }
}
