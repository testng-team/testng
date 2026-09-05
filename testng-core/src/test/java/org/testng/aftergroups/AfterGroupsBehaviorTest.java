package org.testng.aftergroups;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.aftergroups.samples.AfterGroupsSample;
import org.testng.aftergroups.samples.MultipleGroupsSample;
import org.testng.aftergroups.samples.github1362.LocalMethodInterceptor;
import org.testng.aftergroups.samples.github1362.TestSample;
import org.testng.aftergroups.samples.issue165.TestclassSampleWithFailedMember;
import org.testng.aftergroups.samples.issue165.TestclassSampleWithSkippedMember;
import org.testng.aftergroups.samples.issue1880.LocalConfigListener;
import org.testng.aftergroups.samples.issue1880.TestClassSample;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.MethodInstance;
import org.testng.internal.WrappedTestNGMethod;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.FailurePolicy;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.beforegroups.issue2359.ListenerAdapter;

public class AfterGroupsBehaviorTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1880")
  public void ensureAfterGroupsAreInvokedWithAlwaysRunAttribute() {
    runTest(TestClassSample.class, "123", true, "after");
  }

  @Test(dataProvider = "dp", description = "GITHUB-165")
  public void ensureAfterGroupsInvoked(Class<?> clazz, String expected) {
    runTest(clazz, "A", false, expected);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      {TestclassSampleWithSkippedMember.class, "afterGroupsMethod"},
      {TestclassSampleWithFailedMember.class, "afterGroupsMethod"},
    };
  }

  @Test
  public void ensureAfterGroupsInvokedAfterAllTestsWhenMultipleGroupsDefined() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] {MultipleGroupsSample.class});

    ListenerAdapter adapter = new ListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getPassedConfiguration()).hasSize(1);
    ITestResult afterGroup = adapter.getPassedConfiguration().iterator().next();
    adapter
        .getPassedTests()
        .forEach(
            t -> assertThat(t.getEndMillis()).isLessThanOrEqualTo(afterGroup.getStartMillis()));
  }

  @Test
  public void ensureAfterGroupsInvokedWhenTestMethodIsWrappedWithWrappedTestNGMethod() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] {AfterGroupsSample.class});

    tng.setMethodInterceptor(
        (methods, context) -> {
          List<IMethodInstance> result = new ArrayList<>(methods);
          result.add(new MethodInstance(new WrappedTestNGMethod(result.get(0).getMethod())));
          return result;
        });

    ListenerAdapter adapter = new ListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    assertThat(adapter.getPassedConfiguration()).hasSize(1);
  }

  @Test(description = "GITHUB-1362")
  public void ensureAfterGroupsInvokedWhenInterceptorRemovesAGroupMember() {
    runTests(new LocalMethodInterceptor(), "setup", "test1", "test3", "clear");
  }

  @Test(description = "GITHUB-1362")
  public void ensureAfterGroupsInvokedWhenNoInterceptorIsPresent() {
    runTests(null, "setup", "test1", "test2", "test3", "clear");
  }

  private static void runTest(
      Class<?> clazz, String groups, boolean shouldContinue, String expected) {
    XmlSuite xmlsuite = createXmlSuite("sample_suite", "sample_test", clazz);
    xmlsuite.addIncludedGroup(groups);
    TestNG testng = create(xmlsuite);
    if (shouldContinue) {
      testng.setConfigFailurePolicy(FailurePolicy.CONTINUE);
    }
    LocalConfigListener listener = new LocalConfigListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getMessages()).containsExactly(expected);
  }

  private static void runTests(ITestNGListener interceptor, String... names) {
    List<String> expected = Arrays.asList(names);
    XmlSuite xmlsuite = createXmlSuite("suite", "test", TestSample.class);
    xmlsuite.getTests().get(0).setIncludedGroups(Collections.singletonList("exTests"));
    TestNG testng = create(xmlsuite);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    if (interceptor != null) {
      testng.addListener(interceptor);
    }
    testng.run();
    // Every expected method must actually have run, exactly once. The original assertion only
    // checked that each observed name was allowed, which stays true when @AfterGroups never fires
    // at all -- the regression GITHUB-1362 is about.
    assertThat(listener.getInvokedMethodNames()).containsExactlyInAnyOrderElementsOf(expected);
  }
}
