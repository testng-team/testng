package test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class NestedStaticTest extends SimpleBaseTest {

  @Test
  public void nestedClassShouldBeIncluded() {
    TestNG tng = create(NestedStaticSampleTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Set<String> expected = Set.of("nested", "f");
    Set<String> actual = new HashSet<>();
    List<ITestResult> passedTests = tla.getPassedTests();
    for (ITestResult t : passedTests) {
      actual.add(t.getMethod().getMethodName());
    }

    assertThat(actual).isEqualTo(expected);
  }
}
