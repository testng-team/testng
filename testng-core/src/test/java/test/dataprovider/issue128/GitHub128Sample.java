package test.dataprovider.issue128;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Method;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;

public class GitHub128Sample {

  @BeforeClass
  public void beforeClass(ITestContext ctx) {
    assertThat(ctx).isNotNull();
  }

  @BeforeMethod
  public void beforeMethod() {
    // Ensuring that a no-arg configuration method doesn't trigger any errors.
  }

  @Test
  public void testMethod(ITestContext ctx) {
    assertThat(ctx).isNotNull();
  }

  @DataProvider(name = "methods")
  public Object[][] getMethods() throws NoSuchMethodException {
    return new Object[][] {new Object[] {String.class.getMethod("toString"), new Object[0]}};
  }

  @Test(dataProvider = "methods")
  public void testInvokeRemote(Method param1, Object[] param2) {
    fail("The test is supposed to fail because the method param is not defined");
  }

  @Test(dataProvider = "methods")
  public void testInvokeRemote2(@NoInjection Method param1, Object[] param2) {
    assertThat(param1.getName()).isEqualTo("toString");
    assertThat(param2.length).isZero();
  }

  @DataProvider(name = "methods2")
  public Object[][] getMethods2() throws NoSuchMethodException {
    return new Object[][] {new Object[] {new Object[0], String.class.getMethod("toString")}};
  }

  @Test(dataProvider = "methods2")
  public void testInvokeRemoteReversed(Object[] param1, Method param2) {
    fail("The test is supposed to fail because the method param is not defined");
  }

  @Test(dataProvider = "methods2")
  public void testInvokeRemoteReversed2(Object[] param1, @NoInjection Method param2) {
    assertThat(param1.length).isZero();
    assertThat(param2.getName()).isEqualTo("toString");
  }

  // TODO Add similar test for Before/AfterMethod
}
