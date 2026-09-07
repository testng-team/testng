package test.preserveorder;

import org.testng.annotations.Test;

public class Chuck4Sample {

  @Test(
      groups = {"functional"},
      dependsOnMethods = {"c4TestTwo"})
  public void c4TestThree() {}

  @Test(groups = {"functional"})
  public static void c4TestOne() {}

  @Test(
      groups = {"functional"},
      dependsOnMethods = {"c4TestOne"})
  public static void c4TestTwo() {}
}
