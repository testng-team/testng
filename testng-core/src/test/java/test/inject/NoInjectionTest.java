package test.inject;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
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
    assertThat(m.getName()).isEqualTo("f");
  }

  @Test(dataProvider = "provider")
  public void withInjection(Method m) {
    assertThat(m.getName()).isEqualTo("withInjection");
  }
}

class CC {

  public void f() {}
}
