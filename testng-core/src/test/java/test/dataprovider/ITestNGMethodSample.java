package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ITestNGMethodSample {

  @DataProvider(name = "dp1")
  public Object[][] createData(ITestNGMethod m) {
    assertThat(m.getMethodName()).isEqualTo("test1");
    assertThat(m.getConstructorOrMethod().getMethod().getName()).isEqualTo("test1");
    assertThat(m.getRealClass()).isEqualTo(ITestNGMethodSample.class);

    return new Object[][] {{"Cedric"}, {"Alois"}};
  }

  @Test(dataProvider = "dp1")
  public void test1(String s) {}
}
