package test.dataprovider.issue128;

import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;

public class GitHub128Sample {

  @BeforeClass
  public void beforeClass(ITestContext ctx) {
    Assert.assertNotNull(ctx);
  }

  @BeforeMethod
  public void beforeMethod() {
    // Ensuring that a no-arg configuration method doesn't trigger any errors.
  }

  @Test
  public void testMethod(ITestContext ctx) {
    Assert.assertNotNull(ctx);
  }

  @DataProvider(name = "methods")
  public Object[][] getMethods() throws NoSuchMethodException {
    return new Object[][] {new Object[] {String.class.getMethod("toString"), new Object[0]}};
  }

  @Test(dataProvider = "methods")
  public void testInvokeRemote(Method param1, Object[] param2) {
    Assert.fail("The test is supposed to fail because the method param is not defined");
    ;
  }

  @Test(dataProvider = "methods")
  public void testInvokeRemote2(@NoInjection Method param1, Object[] param2) {
    Assert.assertEquals(param1.getName(), "toString");
    Assert.assertEquals(param2.length, 0);
  }

  @DataProvider(name = "methods2")
  public Object[][] getMethods2() throws NoSuchMethodException {
    return new Object[][] {new Object[] {new Object[0], String.class.getMethod("toString")}};
  }

  @Test(dataProvider = "methods2")
  public void testInvokeRemoteReversed(Object[] param1, Method param2) {
    Assert.fail("The test is supposed to fail because the method param is not defined");
  }

  @Test(dataProvider = "methods2")
  public void testInvokeRemoteReversed2(Object[] param1, @NoInjection Method param2) {
    Assert.assertEquals(param1.length, 0);
    Assert.assertEquals(param2.getName(), "toString");
  }

  // TODO Add similar test for Before/AfterMethod
}
