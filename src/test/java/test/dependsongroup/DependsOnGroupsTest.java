package test.dependsongroup;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class DependsOnGroupsTest extends SimpleBaseTest {

  @Test
  public void methodsShouldBeGroupedByClasses() {
    TestNG tng = create(new Class[] {
        ZeroSampleTest.class, FirstSampleTest.class, SecondSampleTest.class
    });

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    String[] expected = new String[] {
        "zeroA", "zeroB",
        "firstA", "firstB",
        "secondA", "secondB"
    };
    for (int i = 0; i < expected.length; i++) {
      ITestResult testResult = tla.getPassedTests().get(i);
      Assert.assertEquals(testResult.getMethod().getMethodName(), expected[i]);
    }
  }
}
