package test.inject;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;

public class Sample {

  @Test
  public void f(ITestContext tc) {
    Assert.assertNotNull(tc);
    ITestNGMethod[] allMethods = tc.getAllTestMethods();
    Assert.assertEquals(allMethods.length, 1);
    Assert.assertEquals(allMethods[0].getMethod().getName(),"f");

  }
}
