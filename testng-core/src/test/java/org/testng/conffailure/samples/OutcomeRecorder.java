package org.testng.conffailure.samples;

import java.util.ArrayList;
import java.util.List;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Records one line per configuration and test result, in the order TestNG reports them: {@code
 * CONFIG PASS setup}, {@code TEST SKIP t}. A skipped configuration is reported like a run one, so
 * the log says which methods TestNG decided about, not only which ones ran.
 */
public class OutcomeRecorder implements IConfigurationListener, ITestListener {

  private final List<String> outcomes = new ArrayList<>();

  public List<String> getOutcomes() {
    return outcomes;
  }

  @Override
  public void onConfigurationSuccess(ITestResult tr) {
    record("CONFIG PASS", tr);
  }

  @Override
  public void onConfigurationFailure(ITestResult tr) {
    record("CONFIG FAIL", tr);
  }

  @Override
  public void onConfigurationSkip(ITestResult tr) {
    record("CONFIG SKIP", tr);
  }

  @Override
  public void onTestSuccess(ITestResult tr) {
    record("TEST PASS", tr);
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    record("TEST FAIL", tr);
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    record("TEST SKIP", tr);
  }

  private void record(String outcome, ITestResult tr) {
    outcomes.add(outcome + " " + tr.getMethod().getMethodName());
  }
}
