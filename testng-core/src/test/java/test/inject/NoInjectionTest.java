package test.inject;

import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;

/**
 * Test the @NoInjection annotation.
 *
 * @author cbeust
 */
public class NoInjectionTest {

  @DataProvider(name = "provider")
  public Object[][] provide() throws Exception {
    return new Object[][] {{CC.class.getMethod("f")}};
  }

  @Test(dataProvider = "provider")
  public void withoutInjection(@NoInjection Method m) {
    Assert.assertEquals(m.getName(), "f");
  }

  @Test(dataProvider = "provider")
  public void withInjection(Method m) {
    Assert.assertEquals(m.getName(), "withInjection");
  }
}

class CC {

  public void f() {}
}
