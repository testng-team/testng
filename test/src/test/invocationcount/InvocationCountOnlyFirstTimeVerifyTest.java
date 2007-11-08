package test.invocationcount;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InvocationCountOnlyFirstTimeVerifyTest {
  @Test(dependsOnGroups = "first")
  public void verify() {
    Assert.assertEquals(InvocationCountOnlyFirstTimeTest.m_beforeCount, 0);
    Assert.assertEquals(InvocationCountOnlyFirstTimeTest.m_afterCount, 9);
  }
}
