package test.timeout;

import org.testng.annotations.Test;

public class GitHub1314Sample {

  @Test
  private void iWorkWell() {
    System.out.println("Test1");
  }

  @Test(
      timeOut = 100,
      dependsOnMethods = {"iWorkWell"})
  private void iHangHorribly() {
    System.out.println("Test2");
    while (true) {
      int two = 1 + 1;
    }
  }

  @Test(dependsOnMethods = {"iHangHorribly"})
  private void iAmNeverRun() {
    System.out.println("Test3");
  }
}
