package test.methodselectors;

import org.testng.annotations.Test;

public class PrioritySampleTest {

  private static void ppp(String s) {
    System.out.println("[PrioritySampleTest] " + s);
  }

  @Test
  public void alwaysRun() {
    ppp("ALWAYS");
  }

  @Test
  @NoTest
  public void neverRun() {
    ppp("NEVER");
  }
}
