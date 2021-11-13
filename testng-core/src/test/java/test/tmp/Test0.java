package test.tmp;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class Test0 {

  @BeforeTest
  public void setup() {
    System.out.println("setup");
  }

  @Test(groups = {"G0"})
  public void test() {
    System.out.println("Test0.test");
  }

  @AfterTest
  public void tearDown() {
    System.out.println("tearDown");
  }
}
