package test.name;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class NameTest extends SimpleBaseTest {

  @Test
  public void itestTest() {
    TestNG tng = create(SimpleITestSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("NAME");
    assertThat(result.getTestName()).isEqualTo("NAME");
  }

  @Test
  public void itestTestWithXml() {
    TestNG tng = createTests("suite", SimpleITestSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("NAME");
    assertThat(result.getTestName()).isEqualTo("NAME");
  }

  @Test
  public void testNameTest() {
    TestNG tng = create(NameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("NAME");
    assertThat(result.getTestName()).isEqualTo("NAME");
  }

  @Test
  public void testNameTestWithXml() {
    TestNG tng = createTests("suite", NameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("NAME");
    assertThat(result.getTestName()).isEqualTo("NAME");
  }

  @Test
  public void noNameTest() {
    TestNG tng = create(NoNameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("test");
    assertThat(result.getTestName()).isNull();
  }

  @Test
  public void noNameTestWithXml() {
    TestNG tng = createTests("suite", NoNameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("test");
    assertThat(result.getTestName()).isNull();
  }

  @Test
  public void complexITestTest() {
    TestNG tng = create(ITestSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(5);
    List<String> testNames =
        new ArrayList<>(Arrays.asList("test1", "test2", "test3", "test4", "test5"));
    for (ITestResult testResult : adapter.getPassedTests()) {
      assertThat(testNames.remove(testResult.getName()))
          .withFailMessage("Duplicate test names " + getNames(adapter.getPassedTests()))
          .isTrue();
    }
    assertThat(testNames).isEqualTo(Collections.emptyList());
  }

  private static List<String> getNames(List<ITestResult> results) {
    List<String> names = new ArrayList<>(results.size());
    for (ITestResult result : results) {
      names.add(result.getName());
    }
    return names;
  }

  @Test(description = "GITHUB-922: ITestResult doesn't contain name if a class has @Test")
  public void testOnClassFromReporter() {
    TestNG tng = create(TestOnClassSample.class);
    TestOnClassListener listener = new TestOnClassListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getNames().size()).isEqualTo(1);
    assertThat(listener.getNames().get(0)).isEqualTo("test");

    // testName should be ignored if not specified
    assertThat(listener.getTestNames().size()).isEqualTo(1);
    assertThat(listener.getTestNames().get(0)).isNull();
  }

  @Test
  public void blankNameTest() {
    TestNG tng = create(BlankNameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("");
    assertThat(result.getTestName()).isEqualTo("");
  }

  @Test
  public void blankNameTestWithXml() {
    TestNG tng = createTests("suite", BlankNameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getFailedTests().isEmpty()).isTrue();
    assertThat(adapter.getSkippedTests().isEmpty()).isTrue();
    assertThat(adapter.getPassedTests().size()).isEqualTo(1);
    ITestResult result = adapter.getPassedTests().get(0);
    assertThat(result.getMethod().getMethodName()).isEqualTo("test");
    assertThat(result.getName()).isEqualTo("");
    assertThat(result.getTestName()).isEqualTo("");
  }
}
