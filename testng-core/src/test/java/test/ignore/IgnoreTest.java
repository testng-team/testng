package test.ignore;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.InvokedMethodListener;
import test.SimpleBaseTest;
import test.ignore.ignorePackage.IgnorePackageSample;
import test.ignore.ignorePackage.subPackage.SubPackageSample;
import test.ignore.issue2396.FirstTest;
import test.ignore.issue2613.LocalClassListener;
import test.ignore.issue2613.SampleTestClassWithEnabledDisabledIgnoredTestMethods;
import test.ignore.issue2613.SampleTestClassWithEnabledIgnoredTestMethods;

public class IgnoreTest extends SimpleBaseTest {

  @Test
  public void ignore_class_should_not_run_tests() {
    InvokedMethodListener listener = runTest(IgnoreClassSample.class);
    assertThat(listener.getInvokedMethods()).isEmpty();
  }

  @Test
  public void ignore_class_with_inheritance_should_not_run_tests() {
    InvokedMethodListener listener = runTest(ChildSample.class);
    assertThat(listener.getInvokedMethods()).isEmpty();
  }

  @Test
  public void ignore_test_should_not_run_test() {
    InvokedMethodListener listener = runTest(IgnoreTestSample.class);
    assertThat(listener.getInvokedMethods()).containsExactly("test");
  }

  @Test
  public void ignore_package_should_not_run_test() {
    InvokedMethodListener listener = runTest(IgnorePackageSample.class, SubPackageSample.class);
    assertThat(listener.getInvokedMethods()).isEmpty();
  }

  @Test(description = "GITHUB-1709")
  public void test_parent_class_tests_ignored_when_ignored_at_child() {
    InvokedMethodListener listener = runTest(ChildClassTestSample.class);
    assertThat(listener.getInvokedMethods()).isEmpty();
  }

  @Test(description = "GITHUB-2396")
  public void test_ignore_happens_for_class_level_methods() {
    InvokedMethodListener listener = runTest(FirstTest.class);
    assertThat(listener.getInvokedMethods()).containsExactly("testShouldBeInvoked");
  }

  @Test(description = "GITHUB-2613", dataProvider = "getTestData")
  public void test_ignored_test_methods_are_retrieved_in_mixed_test_class(
      Class<?> cls, String[] expected) {
    TestNG tng = create(cls);
    LocalClassListener listener = new LocalClassListener();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getMethods()).containsExactlyInAnyOrder(expected);
  }

  @DataProvider(name = "getTestData")
  public Object[][] getTestData() {
    return new Object[][] {
      {
        SampleTestClassWithEnabledDisabledIgnoredTestMethods.class,
        new String[] {"enabledTest", "disabledTest", "ignoredTest"}
      },
      {
        SampleTestClassWithEnabledIgnoredTestMethods.class,
        new String[] {"enabledTest", "ignoredTest"}
      }
    };
  }

  private static InvokedMethodListener runTest(Class<?>... classes) {
    TestNG tng = create(classes);
    InvokedMethodListener listener = new InvokedMethodListener();
    tng.addListener(listener);
    tng.run();
    return listener;
  }
}
