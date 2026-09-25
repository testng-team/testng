package org.testng.conffailure.issue3533;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.OutcomeRecorder;
import org.testng.conffailure.samples.ParallelMethodConfigIsolationSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/** GITHUB-3533: a method-level configuration failure stays on its own data-provider row. */
public class Issue3533Test extends SimpleBaseTest {

  @Test(timeOut = 20_000)
  public void aParallelSiblingRowIsNotSkippedByTheOtherRowsSetupFailure() {
    ParallelMethodConfigIsolationSample.reset();
    TestNG testng = create(ParallelMethodConfigIsolationSample.class);
    testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    testng.setDataProviderThreadCount(2);
    OutcomeRecorder recorder = new OutcomeRecorder();
    testng.addListener(recorder);
    testng.addListener(new ParallelMethodConfigIsolationSample.Gate());
    testng.run();

    List<String> outcomes = recorder.getOutcomes();
    assertThat(outcomes).contains("CONFIG FAIL setup", "CONFIG PASS setup", "TEST SKIP t(0)");
    assertThat(outcomes).contains("TEST PASS t(1)");
    assertThat(outcomes).doesNotContain("TEST SKIP t(1)", "CONFIG SKIP setup");
  }
}
