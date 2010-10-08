package test.dataprovider;

import org.testng.annotations.DataProvider;

public class InheritanceATest {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
        new Object[] { "a"}
    };
  }


}
