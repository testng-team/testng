package test.inject;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

public class InjectTestContextTest extends SimpleBaseTest {

  @Test(enabled = false)
  public void verifyTestContextInjection(ITestContext tc, XmlTest xmlTest) {
    TestNG tng = create();
    tng.setTestClasses(new Class[] { Sample.class });
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(xmlTest.getName(), "Injection");
    Assert.assertEquals(tla.getPassedTests().size(), 1);
    Assert.assertEquals(tla.getPassedTests().get(0).getMethod().getMethodName(), "f");
  }

  @Parameters("string")
  @Test(enabled = true)
  public void injectionAndParameters(String s, ITestContext ctx) {
  }
}
