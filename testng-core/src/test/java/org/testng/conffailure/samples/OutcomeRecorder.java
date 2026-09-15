package org.testng.conffailure.samples;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Records one line per configuration and test result, in the order TestNG reports them: {@code
 * CONFIG PASS setup}, {@code TEST SKIP t(1)}. A skipped configuration is reported like a run one,
 * so the log says which methods TestNG decided about, not only which ones ran. A test result
 * carries its parameters, so the rows of a data provider can be told apart.
 */
public class OutcomeRecorder implements IConfigurationListener, ITestListener {

  private final List<String> outcomes = new CopyOnWriteArrayList<>();

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
    recordTest("TEST PASS", tr);
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    recordTest("TEST FAIL", tr);
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    recordTest("TEST SKIP", tr);
  }

  private void record(String outcome, ITestResult tr) {
    outcomes.add(outcome + " " + tr.getMethod().getMethodName());
  }

  private void recordTest(String outcome, ITestResult tr) {
    Object[] parameters = tr.getParameters();
    String suffix =
        parameters.length == 0
            ? ""
            : Arrays.stream(parameters)
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "(", ")"));
    outcomes.add(outcome + " " + tr.getMethod().getMethodName() + suffix);
  }
}
