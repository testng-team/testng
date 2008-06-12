package test.inject;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class InjectTestContextTest extends SimpleBaseTest {

  @Test
  public void verifyTestContextInjection(ITestContext tc) {
    TestNG tng = create();
    tng.setTestClasses(new Class[] { Sample.class });
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), 1);
    Assert.assertEquals(tla.getPassedTests().get(0).getMethod().getMethodName(), "f");
  }

}
