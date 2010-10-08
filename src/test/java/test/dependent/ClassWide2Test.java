package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ClassWide2Test {

//  @Test(dependsOnMethods = {"m1" })
  @Test(dependsOnMethods = {"test.dependent.ClassWide1Test.m1" })
  public void m2() {
    Assert.assertTrue(ClassWide1Test.m1WasRun());
  }

//  @Test
//  public void m1() {
//
//  }

}
