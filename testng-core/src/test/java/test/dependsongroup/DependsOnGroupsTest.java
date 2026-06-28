package test.dependsongroup;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class DependsOnGroupsTest extends SimpleBaseTest {

  @Test
  public void methodsShouldBeGroupedByClasses() {
    TestNG tng = create(ZeroSampleTest.class, FirstSampleTest.class, SecondSampleTest.class);

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    String[] expected =
        new String[] {
          "zeroA", "zeroB",
          "firstA", "firstB",
          "secondA", "secondB"
        };
    for (int i = 0; i < expected.length; i++) {
      ITestResult testResult = tla.getPassedTests().get(i);
      assertThat(testResult.getMethod().getMethodName()).isEqualTo(expected[i]);
    }
  }
}
