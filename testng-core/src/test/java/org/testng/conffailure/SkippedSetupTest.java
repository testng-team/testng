package org.testng.conffailure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.OutcomeRecorder;
import org.testng.conffailure.samples.skippedsetup.GroupSetupOwnerSample;
import org.testng.conffailure.samples.skippedsetup.GroupSetupPeerSample;
import org.testng.conffailure.samples.skippedsetup.IgnoredClassFailureSample;
import org.testng.conffailure.samples.skippedsetup.RegressionClassSample;
import org.testng.conffailure.samples.skippedsetup.SkippedMethodSetupSample;
import org.testng.conffailure.samples.skippedsetup.SmokeGroupSample;
import test.SimpleBaseTest;

/**
 * A configuration method skipped because an earlier one failed still marks what it would have set
 * up: a test that needs that setup is skipped too, exactly as when the setup does not say {@code
 * alwaysRun}. What the skip must not do is move the level flags that decide where {@code
 * ignoreFailure} is looked up for a later class; only a failure does that.
 */
public class SkippedSetupTest extends SimpleBaseTest {

  @Test
  public void aTestWhoseBeforeGroupsWasSkippedIsSkipped() {
    // The peer's test is in group g and the group's setup never ran, so it is skipped: the same
    // answer master gives when the setup does not say alwaysRun.
    assertThat(outcomesOf(GroupSetupOwnerSample.class, GroupSetupPeerSample.class))
        .containsExactly(
            "CONFIG FAIL beforeClassA",
            "CONFIG SKIP beforeGroupsG",
            "TEST SKIP testA",
            "TEST SKIP testB");
  }

  @Test
  public void aTestWhoseBeforeClassWasSkippedIsSkipped() {
    // The class setup lists the failed group and is skipped for it; the test outside that group
    // still needs the class setup, so it is skipped too.
    assertThat(outcomesOf(SmokeGroupSample.class, RegressionClassSample.class))
        .containsExactly(
            "CONFIG FAIL beforeSmoke",
            "TEST SKIP smokeTest",
            "CONFIG SKIP beforeClassB",
            "TEST SKIP regressionTest");
  }

  @Test
  public void aSkippedBeforeMethodDoesNotSetTheMethodLevelFlag() {
    // Alone, c passes: {@code ignoreFailure} on its own @BeforeClass covers it. A skipped
    // @BeforeMethod in an earlier class must not change which list it is looked up in.
    assertThat(outcomesOf(SkippedMethodSetupSample.class, IgnoredClassFailureSample.class))
        .containsExactly(
            "CONFIG FAIL beforeClassD",
            "CONFIG SKIP beforeMethodD",
            "TEST SKIP d",
            "CONFIG FAIL beforeClassC",
            "TEST PASS c");
  }

  private static List<String> outcomesOf(Class<?>... classes) {
    TestNG tng = create(classes);
    OutcomeRecorder recorder = new OutcomeRecorder();
    tng.addListener(recorder);
    tng.run();
    return recorder.getOutcomes();
  }
}
