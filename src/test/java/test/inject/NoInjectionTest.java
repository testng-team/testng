package test.inject;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * Test the @NoInjection annotation.
 *
 * @author cbeust
 */
public class NoInjectionTest {
  public void f() {
  }

  @DataProvider(name = "singleValueProvider")
  public Object[][] provide() throws Exception {
      return new Object[][] { { NoInjectionTest.class.getMethod("f") } };
  }

  @Test(dataProvider = "singleValueProvider")
  public void withoutInjection(@NoInjection Method m) {
      Assert.assertEquals(m.getName(), "f");
  }

  @Test(dataProvider = "singleValueProvider")
  public void withInjection(Method m) {
      Assert.assertEquals(m.getName(), "withInjection");
  }

  // Multi-injection test
  @DataProvider
  public Object[][] multiValuedProvider() throws NoSuchMethodException {
    return new Object[][] {
      { null, "some_data" }
    };
  }

  @Test(dataProvider = "multiValuedProvider")
  public void multiValuedTest1(@NoInjection Method providedMethod, Method thisMethod, String data) {
    Assert.assertNull(providedMethod);
    Assert.assertEquals(thisMethod.getName(), "multiValuedTest1");
    Assert.assertEquals(data, "some_data");
  }

  @Test(dataProvider = "multiValuedProvider")
  public void multiValuedTest2(Method thisMethod, @NoInjection Method providedMethod, String data) {
    Assert.assertNull(providedMethod);
    Assert.assertEquals(thisMethod.getName(), "multiValuedTest2");
    Assert.assertEquals(data, "some_data");
  }
}
