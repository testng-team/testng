package test.dataprovider;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class TestNG411Test {

  @Test
  public void verify() {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { TestNG411SampleTest.class });
    TestListenerAdapter al = new TestListenerAdapter();
    tng.addListener(al);
    tng.run();

    Assert.assertEquals(al.getPassedTests().size(), 1);
    Assert.assertEquals(al.getPassedTests().get(0).getMethod().getMethodName(), "checkMinTest_injection");

    Assert.assertEquals(al.getFailedTests().size(), 2);
  }
}
