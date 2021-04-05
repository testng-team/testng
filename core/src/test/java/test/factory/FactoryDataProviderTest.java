package test.factory;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.Iterator;

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

    Assert.assertEquals(tla.getPassedTests().size(), 2);
    Iterator<ITestResult> iterator = tla.getPassedTests().iterator();
    BaseFactorySample t1 = (BaseFactorySample) iterator.next().getInstance();
    BaseFactorySample t2 = (BaseFactorySample) iterator.next().getInstance();
    Assert.assertEquals(t1.getN(), n1);
    Assert.assertEquals(t2.getN(), n2);
  }
}
