package org.testng.internal.invokers;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.CountMovesBetweenRecordAndLookup;
import org.testng.conffailure.samples.OutcomeRecorder;
import org.testng.conffailure.samples.ParallelAfterMethodIsolationSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * GITHUB-3533: the invocation token must not re-read the shared counter between recording a
 * configuration failure and looking it up.
 */
public class ConfigFailureTokenTest extends SimpleBaseTest {

  @Test(timeOut = 20_000)
  public void aFailureRecordedBeforeTheSharedCountMovesIsStillFound() {
    ConfigInvoker.afterMethodFailureRecorded = ITestNGMethod::incrementCurrentInvocationCount;
    try {
      TestNG testng = create(CountMovesBetweenRecordAndLookup.class);
      testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
      OutcomeRecorder recorder = new OutcomeRecorder();
      testng.addListener(recorder);
      testng.run();

      assertThat(recorder.getOutcomes())
          .containsExactlyInAnyOrder("CONFIG FAIL setup", "TEST SKIP t");
    } finally {
      ConfigInvoker.afterMethodFailureRecorded = null;
    }
  }

  @Test(timeOut = 20_000)
  public void anAfterMethodFailureDoesNotSkipTheSiblingRow() {
    ParallelAfterMethodIsolationSample.reset();
    ConfigInvoker.afterMethodFailureRecorded =
        method -> {
          if ("t".equals(method.getMethodName())) {
            ParallelAfterMethodIsolationSample.tornDown.countDown();
          }
        };
    try {
      TestNG testng = create(ParallelAfterMethodIsolationSample.class);
      testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
      testng.setDataProviderThreadCount(2);
      OutcomeRecorder recorder = new OutcomeRecorder();
      testng.addListener(recorder);
      testng.run();

      assertThat(recorder.getOutcomes())
          .containsExactlyInAnyOrder(
              "CONFIG PASS setup",
              "CONFIG PASS setup",
              "CONFIG FAIL teardown",
              "CONFIG PASS teardown",
              "TEST PASS t(0)",
              "TEST PASS t(1)");
    } finally {
      ConfigInvoker.afterMethodFailureRecorded = null;
    }
  }
}
