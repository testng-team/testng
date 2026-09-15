package org.testng.conffailure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.conffailure.samples.ClassWithFailedBeforeTestClassVerification.success;

import org.jspecify.annotations.Nullable;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.ClassWithFailedBeforeSuite;
import org.testng.conffailure.samples.ClassWithFailedBeforeSuiteVerification;
import org.testng.conffailure.samples.ClassWithFailedBeforeTestClass;
import org.testng.conffailure.samples.ClassWithFailedBeforeTestClassVerification;
import org.testng.conffailure.samples.OutcomeRecorder;
import org.testng.conffailure.samples.PlainAfterTestSample;
import org.testng.conffailure.samples.github990.AbstractBaseSample;
import org.testng.conffailure.samples.github990.ChildClassSample;
import org.testng.testhelper.OutputDirectoryPatch;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * Test various cases where the @Configuration methods fail
 *
 * <p>Created on Jul 20, 2005
 *
 * @author cbeust
 */
public class ConfigurationFailure extends SimpleBaseTest {

  @Test
  public void beforeTestClassFails() {
    runTest(ClassWithFailedBeforeTestClass.class, ClassWithFailedBeforeTestClassVerification.class);
    assertThat(success())
        .withFailMessage("Not all the @Configuration methods of Run2 were run")
        .isTrue();
  }

  @Test
  public void beforeTestSuiteFails() {
    runTest(ClassWithFailedBeforeSuite.class, ClassWithFailedBeforeSuiteVerification.class);
    assertThat(ClassWithFailedBeforeSuiteVerification.success())
        .withFailMessage("No @Configuration methods should have run")
        .isTrue();
  }

  private static void runTest(Class<?>... classes) {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create(classes);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.addListener(tla);
    testng.run();
  }

  @DataProvider
  public static Object[][] plainAfterTestOutcomes() {
    // A test-level configuration has no test instance. The failure check reads the records of
    // the class for that instance; it must answer "no record" for a null one, not throw, or the
    // teardown is reported as failed instead of what the policy says. Both lines are what master
    // answers.
    return new Object[][] {
      {null, new String[] {"CONFIG FAIL setup", "TEST SKIP execute", "CONFIG SKIP cleanup"}},
      {
        XmlSuite.FailurePolicy.CONTINUE,
        new String[] {"CONFIG FAIL setup", "TEST PASS execute", "CONFIG PASS cleanup"}
      },
    };
  }

  @Test(dataProvider = "plainAfterTestOutcomes")
  public void afterTestWithoutAlwaysRunFollowsThePolicyAfterAFailedBeforeTest(
      XmlSuite.@Nullable FailurePolicy policy, String[] expected) {
    TestNG testng = create(PlainAfterTestSample.class);
    testng.setConfigFailurePolicy(policy);
    OutcomeRecorder recorder = new OutcomeRecorder();
    testng.addListener(recorder);
    testng.run();
    assertThat(recorder.getOutcomes()).containsExactly(expected);
  }

  @Test(description = "GITHUB-990")
  public void ensureConfigurationRunsFromBaseClass() {
    TestNG testng = create(ChildClassSample.class);
    testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    testng.run();
    assertThat(AbstractBaseSample.messages).containsExactly("cleanup");
  }
}
