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
 * A configuration method skipped because an earlier one failed is not itself a failure. The earlier
 * failure is already recorded; recording the skip as well would reach tests the failure does not
 * cover.
 */
public class SkippedSetupTest extends SimpleBaseTest {

  @Test
  public void aSkippedBeforeGroupsDoesNotMarkItsGroupFailed() {
    assertThat(outcomesOf(GroupSetupOwnerSample.class, GroupSetupPeerSample.class))
        .containsExactly(
            "CONFIG FAIL beforeClassA",
            "CONFIG SKIP beforeGroupsG",
            "TEST SKIP testA",
            "TEST PASS testB");
  }

  @Test
  public void aSkippedBeforeClassDoesNotMarkItsClassFailed() {
    assertThat(outcomesOf(SmokeGroupSample.class, RegressionClassSample.class))
        .containsExactly(
            "CONFIG FAIL beforeSmoke",
            "TEST SKIP smokeTest",
            "CONFIG SKIP beforeClassB",
            "TEST PASS regressionTest");
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
