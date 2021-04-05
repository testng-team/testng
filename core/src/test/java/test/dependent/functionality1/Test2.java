package test.dependent.functionality1;

import org.testng.annotations.Test;

@Test(groups = "tests.functional.upload", dependsOnGroups = "tests.functional.package")
public class Test2 {

  public void test2_1() {
    System.out.println("Test 2_1");
  }

  public void test2_2() {
    System.out.println("Test 2_2");
  }
}
