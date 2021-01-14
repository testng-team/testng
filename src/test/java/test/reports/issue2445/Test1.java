package test.reports.issue2445;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Test1 {
  @Test
  public void test1() {
    System.out.println("Test1 test method");
    Assert.fail();
  }
}