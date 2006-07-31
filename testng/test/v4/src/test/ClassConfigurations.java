package test;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ClassConfigurations {

  static int beforeCount = 0;
  static int afterCount = 0;

  @Configuration(beforeTestClass=true)
  public void beforeTestClass() {
    ++beforeCount;
//    System.out.println("@@@@@@ beforeTestClass has been called " + beforeCount + " time(s)");
  }
  
  @Configuration(afterTest = true)
  public void afterTest() {
    beforeCount = 0;
    afterCount = 0;
  }

  @Configuration(afterTestClass=true)
  public void afterTestClass() {
    ++afterCount;
//    System.out.println("@@@@@@@ afterTestClass has been called " + afterCount + " time(s)");
  }

  @Test
  public void testOne() {
//    System.out.println("testOne");
    assert beforeCount == 1;
    assert afterCount == 0;
  }

  @Test
  public void testTwo() {
//    System.out.println("testTwo");
    assert beforeCount == 1;
    assert afterCount == 0;
  }

  @Test
  public void testThree() {
//    System.out.println("testThree");
    assert beforeCount == 1;
    assert afterCount == 0;
  }
}