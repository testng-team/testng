package test.factory;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test(groups = { "x" })
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
      Assert.assertNotNull(s);
    }
  }

  private Object[] makeNullArgTests(String s) {
    return new Object[0];
  }

  @DataProvider(name = "data")
  public Object[][] makeData() {
    return new Object[][] { { "foo" } };
  };
}
