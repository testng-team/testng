package org.testng.internal;

/**
 * Determine the availability of certain jar files at runtime.
 */
public class Dynamic {

  public static boolean hasBsh() {
    return ClassHelper.forName("bsh.Interpreter") != null;
  }
}
