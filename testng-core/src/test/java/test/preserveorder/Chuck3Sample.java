package test.preserveorder;

import org.testng.annotations.Test;

public class Chuck3Sample {

  @Test(
      groups = {"functional"},
      dependsOnMethods = {"c3TestTwo"})
  public void c3TestThree() {}

  @Test(groups = {"functional"})
  public static void c3TestOne() {}

  @Test(
      groups = {"functional"},
      dependsOnMethods = {"c3TestOne"})
  public static void c3TestTwo() {}
}
