package test.methodselectors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class PriorityTest {

  private void runTest(int priority, String[] passedTests) {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] {PrioritySampleTest.class});
    tng.addMethodSelector("test.methodselectors.NoTestSelector", priority);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    List<ITestResult> passed = tla.getPassedTests();
    assertThat(passedTests.length).isEqualTo(passed.size());
    if (passedTests.length == 1) {
      String passed0 = passed.get(0).getName();
      assertThat(passed0).isEqualTo(passedTests[0]);
    }
    if (passedTests.length == 2) {
      String passed0 = passed.get(0).getName();
      String passed1 = passed.get(1).getName();
      assertThat(passed0).isIn(passedTests[0], passedTests[1]);
      assertThat(passed1).isIn(passedTests[0], passedTests[1]);
    }
  }

  //  @Test
  public void negativePriority() {
    runTest(-5, new String[] {});
  }

  @Test
  public void lessThanTenPriority() {
    runTest(5, new String[] {"alwaysRun"});
  }

  //  @Test
  public void greaterThanTenPriority() {
    runTest(15, new String[] {"alwaysRun", "neverRun"});
  }
}
