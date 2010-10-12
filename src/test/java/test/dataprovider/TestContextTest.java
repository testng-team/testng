package test.dataprovider;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class TestContextTest {

  @Test
  public void verifyTen() {
    verify("10", "verifyTen", 1, 0);
  }

  @Test
  public void verifyFive() {
    verify("5", "verifyFive", 1, 0);
  }

  @Test
  public void verifySix() {
    // Not including any group, so the two test methods should fail
    verify(null, null, 0, 2);
  }

  private void verify(String groupName, String passed, int passedCount, int failedCount) {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { TestContextSampleTest.class });
    if (groupName != null) {
      tng.setGroups(groupName);
    }
    TestListenerAdapter al = new TestListenerAdapter();
    tng.addListener(al);
    tng.run();

    if (passedCount > 0) {
      Assert.assertEquals(al.getPassedTests().size(), passedCount);
      Assert.assertEquals(al.getPassedTests().get(0).getMethod().getMethodName(), passed);
    }

    if (failedCount > 0) {
      Assert.assertEquals(al.getFailedTests().size(), failedCount);
    }
  }
}
