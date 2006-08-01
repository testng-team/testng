package test.tmp;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class Test0 {

  @Configuration(beforeTest = true)
  public void setup() {
          System.out.println("setup");
  }

  @Test(groups = { "G0" })
  public void test() {
          System.out.println("Test0.test");
  }

  @Configuration(afterTest = true)
  public void tearDown() {
          System.out.println("tearDown");
  }

}