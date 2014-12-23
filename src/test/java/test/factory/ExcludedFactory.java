package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Dummy class with @Factory ctor that shouldn't run.
 * @author beverage
 */
public class ExcludedFactory {

  private static boolean factoryRan = false;

  @Factory(dataProvider = "empty")
  public ExcludedFactory(int a) {
    factoryRan = true;
  }

  @Test(groups = "excludedCtorFactory")
  public void emptyTest() {
  }
  
  public static boolean didFactoryRun() {
    return factoryRan;
  }

  @SuppressWarnings("unused")
  @DataProvider(name = "empty")
  private static Object[][] provider() {
    return new Object[][] { { 1 } };
  }
}
