package org.testng.conffailure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.OutcomeRecorder;
import org.testng.conffailure.samples.retry.ChildSetupSample;
import org.testng.conffailure.samples.retry.DataProviderRowsSample;
import org.testng.conffailure.samples.retry.TeardownFailsOnceSample;
import test.SimpleBaseTest;

/**
 * An attempt that retries a failed test method is not held back by what that failed attempt
 * recorded, and by nothing else: a failure during the retry still counts, and the exemption ends
 * with the retry.
 */
public class RetriedInvocationTest extends SimpleBaseTest {

  @Test
  public void aRetryGetsItsSetupAndItsTeardownBack() {
    // The first attempt's teardown failed and, under the default SKIP policy, marked the class.
    // That mark was recorded before the retry started, so the retry runs its setup, its test
    // method and its teardown. Its test method asserts that the setup did run.
    assertThat(outcomesOf(TeardownFailsOnceSample.class))
        .containsExactly(
            "CONFIG PASS setup",
            "TEST SKIP flaky",
            "CONFIG FAIL teardown",
            "CONFIG PASS setup",
            "TEST PASS flaky",
            "CONFIG PASS teardown");
  }

  @Test
  public void aSetupThatFailsDuringTheRetryStillSkipsWhatFollows() {
    // The parent setup fails on the retry. That failure is recorded after the mark the retry took,
    // so it counts: the child setup and the test method are skipped, as outside a retry.
    assertThat(outcomesOf(ChildSetupSample.class))
        .containsExactly(
            "CONFIG PASS parentSetup",
            "CONFIG PASS childSetup",
            "TEST SKIP flaky",
            "CONFIG FAIL parentSetup",
            "CONFIG SKIP childSetup",
            "TEST SKIP flaky");
  }

  @Test
  public void theExemptionEndsWithTheRetry() {
    // Row 1 is retried, and MethodRunner hands the context of that retry to the rows that follow.
    // Row 2's setup fails; row 2 and row 3 report what they report when row 1 does not fail.
    assertThat(outcomesOf(DataProviderRowsSample.class))
        .containsExactly(
            "CONFIG PASS setup",
            "TEST SKIP t",
            "CONFIG PASS setup",
            "TEST PASS t",
            "CONFIG FAIL setup",
            "TEST SKIP t",
            "CONFIG SKIP setup",
            "TEST SKIP t");
  }

  private static List<String> outcomesOf(Class<?>... classes) {
    TestNG tng = create(classes);
    OutcomeRecorder recorder = new OutcomeRecorder();
    tng.addListener(recorder);
    tng.run();
    return recorder.getOutcomes();
  }
}
