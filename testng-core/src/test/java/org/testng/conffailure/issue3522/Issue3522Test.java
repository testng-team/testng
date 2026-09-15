package org.testng.conffailure.issue3522;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.OutcomeRecorder;
import org.testng.conffailure.samples.issue3522.ClassFailureWithIgnoredMethodSample;
import org.testng.conffailure.samples.issue3522.EarlierMethodFailureSample;
import org.testng.conffailure.samples.issue3522.FactoryMixedFailureSample;
import org.testng.conffailure.samples.issue3522.InterfaceClassFailureSample;
import org.testng.conffailure.samples.issue3522.InterfaceMethodFailureSample;
import org.testng.conffailure.samples.issue3522.SiblingClassFailureSample;
import org.testng.conffailure.samples.issue3522.SiblingMethodFailureSample;
import test.SimpleBaseTest;

/**
 * {@code ignoreFailure} on {@code @BeforeMethod} must not let a later class run a test after that
 * class's own {@code @BeforeClass} failed. The lookup flags that pick which configuration list to
 * scan are per test class and instance, not per {@code <test>}, and not the declaring type of the
 * configuration method.
 */
public class Issue3522Test extends SimpleBaseTest {

  @Test(description = "GITHUB-3522")
  public void ignoreFailureOnBeforeMethodDoesNotLetALaterClassRunWithoutSetup() {
    assertThat(
            outcomesOf(EarlierMethodFailureSample.class, ClassFailureWithIgnoredMethodSample.class))
        .containsExactly(
            "CONFIG FAIL zSetup",
            "TEST SKIP zTest",
            "CONFIG FAIL aClass",
            "CONFIG SKIP aSetup",
            "TEST SKIP aTest");
  }

  @Test(description = "GITHUB-3522")
  public void aClassFailureAloneStillSkipsTheTest() {
    assertThat(outcomesOf(ClassFailureWithIgnoredMethodSample.class))
        .containsExactly("CONFIG FAIL aClass", "CONFIG SKIP aSetup", "TEST SKIP aTest");
  }

  @Test(description = "GITHUB-3522")
  public void aSharedBaseBeforeMethodDoesNotLeakIgnoreFailureToASibling() {
    assertThat(outcomesOf(SiblingMethodFailureSample.class, SiblingClassFailureSample.class))
        .containsExactly(
            "CONFIG FAIL sharedSetup",
            "TEST SKIP zTest",
            "CONFIG FAIL aClass",
            "CONFIG SKIP sharedSetup",
            "TEST SKIP aTest");
  }

  @Test(description = "GITHUB-3522")
  public void anInterfaceBeforeMethodFailureDoesNotLetAnotherImplementorRunWithoutSetup() {
    assertThat(outcomesOf(InterfaceMethodFailureSample.class, InterfaceClassFailureSample.class))
        .containsExactly(
            "CONFIG FAIL ifaceSetup",
            "TEST SKIP zTest",
            "CONFIG FAIL aClass",
            "CONFIG SKIP ifaceSetup",
            "TEST SKIP aTest");
  }

  @Test(description = "GITHUB-3522")
  public void aFactoryInstanceBeforeMethodFailureDoesNotLetAPeerRunWithoutSetup() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create(FactoryMixedFailureSample.class);
    testng.addListener(tla);
    testng.run();

    assertThat(tla.getPassedTests()).isEmpty();
    assertThat(tla.getSkippedTests())
        .extracting(Issue3522Test::factoryId)
        .containsExactlyInAnyOrder("method", "class");
  }

  private static List<String> outcomesOf(Class<?>... classes) {
    TestNG tng = create(classes);
    OutcomeRecorder recorder = new OutcomeRecorder();
    tng.addListener(recorder);
    tng.run();
    return recorder.getOutcomes();
  }

  private static String factoryId(ITestResult result) {
    return ((FactoryMixedFailureSample) result.getInstance()).id();
  }
}
