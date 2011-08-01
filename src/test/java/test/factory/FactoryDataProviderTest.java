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
    runTest(FactoryDataProviderSampleTest.class, 41, 42);
  }
  
  @Test(description = "Test @Factory(dataProvider) on a data provider in another class")
  public void factoryWithStaticDataProvider() {
    runTest(FactoryDataProviderStaticSampleTest.class, 43, 44);
  }

  @Test(description = "Test @Factory(dataProvider) on a non static data provider with no arg ctor")
  public void factoryWithNonStaticDataProvider() {
    runTest(FactoryDataProviderWithNoArgCtorSampleErrorTest.class, 45, 46);
  }

  @Test(expectedExceptions = TestNGException.class,
      description = "Should fail because the data provider is not static")
  public void factoryWithNonStaticDataProviderShouldFail() {
    runTest(FactoryDataProviderStaticSampleErrorTest.class, 43, 44);
  }

  private void runTest(Class<?> cls, int n1, int n2) {
    TestNG tng = create(cls);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 2);
    Iterator<ITestResult> iterator = tla.getPassedTests().iterator();
    BaseFactory t1 = (BaseFactory) iterator.next().getInstance();
    BaseFactory t2 = (BaseFactory) iterator.next().getInstance();
//    Assert.assertTrue(t1.getN() == n1 || t1.getN() == n2);
//    Assert.assertTrue(t2.getN() == n1 || t2.getN() == n2);
//    System.out.println("Results:" + t1.getN() + " " + t2.getN());
    Assert.assertEquals(t1.getN(), n1);
    Assert.assertEquals(t2.getN(), n2);
  }
}
