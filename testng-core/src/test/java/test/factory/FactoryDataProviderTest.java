package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class FactoryDataProviderTest extends SimpleBaseTest {

  @Test(description = "Test @Factory(dataProvider) on a local static data provider")
  public void factoryWithLocalDataProvider() {
    runTest(FactoryDataProviderSample.class, 41, 42);
  }

  @Test(description = "Test @Factory(dataProvider) on a data provider in another class (static)")
  public void factoryWithStaticDataProvider() {
    runTest(FactoryDataProviderStaticSample.class, 43, 44);
  }

  @Test(
      description = "Test @Factory(dataProvider) on a data provider in another class (not static)")
  public void factoryWithNotStaticDataProvider() {
    runTest(FactoryDataProviderNotStaticSample.class, 43, 44);
  }

  @Test(description = "Test @Factory(dataProvider) on a non static data provider with no arg ctor")
  public void factoryWithNonStaticDataProvider() {
    runTest(FactoryDataProviderWithNoArgCtorErrorSample.class, 45, 46);
  }

  @Test(
      expectedExceptions = TestNGException.class,
      description = "Should fail because the data provider is not static")
  public void factoryWithNonStaticDataProviderShouldFail() {
    runTest(FactoryDataProviderStaticErrorSample.class, 43, 44);
  }

  private static void runTest(Class<?> cls, int n1, int n2) {
    TestNG tng = create(cls);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertThat(tla.getPassedTests())
        .describedAs("arguments of passed tests: getPassedTests()")
        .extracting(x -> ((BaseFactorySample) x.getInstance()).getN())
        .isEqualTo(Arrays.asList(n1, n2));
  }
}
