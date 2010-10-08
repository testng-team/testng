package test.methodselectors;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.List;

public class PriorityTest {

  private void runTest(int priority, String[] passedTests) {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { PrioritySampleTest.class });
    tng.addMethodSelector("test.methodselectors.NoTestSelector", priority);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    List<ITestResult> passed = tla.getPassedTests();
    Assert.assertEquals(passedTests.length, passed.size());
    if (passedTests.length == 1) {
      String passed0 = passed.get(0).getName();
      Assert.assertEquals(passed0, passedTests[0]);
    }
    if (passedTests.length == 2) {
      String passed0 = passed.get(0).getName();
      String passed1 = passed.get(1).getName();
      Assert.assertTrue(passed0.equals(passedTests[0])
        || passed0.equals(passedTests[1]));
      Assert.assertTrue(passed1.equals(passedTests[0])
          || passed1.equals(passedTests[1]));

    }
  }

//  @Test
  public void negativePriority() {
    runTest(-5, new String[] {});
  }

  @Test
  public void lessThanTenPriority() {
    runTest(5, new String[] { "alwaysRun" });
  }

//  @Test
  public void greaterThanTenPriority() {
    runTest(15, new String[] { "alwaysRun", "neverRun" });
  }


}
