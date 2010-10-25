package test.factory;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.List;

public class FactoryOrderMainTest extends SimpleBaseTest {

  @Test
  public void factoriesShouldBeInvokedInTheOrderOfCreation() {
    TestNG tng = create(FactoryOrderTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    List<ITestResult> passed = tla.getPassedTests();
    for (int i = 0; i < passed.size(); i++) {
      Assert.assertEquals(((FactoryOrderSampleTest) passed.get(i).getInstance()).getValue(), i);
    }
  }
}
