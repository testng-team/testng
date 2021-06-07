package test.listeners.github2522;

import org.testng.annotations.Test;

public class SkipTestSample {
  private static boolean flag = true;

  @Test()
  public void oneTest() {
    flag = false;
  }

  @Test()
  public void twoTest() {}

  public static boolean getFlag() {
    return flag;
  }
}
