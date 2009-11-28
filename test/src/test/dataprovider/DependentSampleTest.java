package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DependentSampleTest {
  @DataProvider(name = "data")
  public Object[][] dp() {
      return new Object[][] { { "ok" }, { "not ok" }, };
  }

  @Test(groups = { "a" }, dataProvider = "data")
  public void method1(String s) {
      if (!"ok".equals(s)) {
          throw new RuntimeException("error " + s);
      }
  }

  @Test(groups = { "b" }, dependsOnGroups = { "a" })
  public void method2() throws InterruptedException {
  }
}
