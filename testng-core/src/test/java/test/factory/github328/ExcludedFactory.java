package test.factory.github328;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ExcludedFactory {

  public static final String EXCLUDED_GROUP = "excludedCtorFactory";
  public static boolean factoryRan = false;

  @Factory(dataProvider = "empty")
  public ExcludedFactory(int a) {
    factoryRan = true;
  }

  @Test(groups = EXCLUDED_GROUP)
  public void emptyTest() {}

  @DataProvider(name = "empty")
  private static Object[][] provider() {
    return new Object[][] {{1}};
  }
}
