package org.testng.internal;

/**
 * Determine the availability of certain jar files at runtime.
 */
public class Dynamic {

  public static boolean hasBsh() {
    try {
      Class.forName("bsh.Interpreter");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
