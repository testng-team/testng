package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test(groups = {"x"})
public class FactoryAndTestMethodTest {

  @Factory(dataProvider = "data")
  public Object[] ohNo(String s) {
    return makeNullArgTests(s);
  }

  public static class NullArgsTest {
    public final String s;

    public NullArgsTest(String s) {
      this.s = s;
    }

    @Test
    public void test() {
      assertThat(s).isNotNull();
    }
  }

  private Object[] makeNullArgTests(String s) {
    return new Object[0];
  }

  @DataProvider(name = "data")
  public Object[][] makeData() {
    return new Object[][] {{"foo"}};
  }
}
