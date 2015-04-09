package test;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NestedStaticTest extends SimpleBaseTest {

  @Test
  public void nestedClassShouldBeIncluded() {
    TestNG tng = create(NestedStaticSampleTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Set<String> expected = new HashSet<String>() {{
      add("nested");
      add("f");
    }};
    Set<String> actual = new HashSet<>();
    List<ITestResult> passedTests = tla.getPassedTests();
    for (ITestResult t : passedTests) {
      actual.add(t.getMethod().getMethodName());
    }

    Assert.assertEquals(actual, expected);
  }
}
