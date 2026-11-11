package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

/** Records the outcome of every configuration method, in the order TestNG reports it. */
public class RecordingConfigListener implements IConfigurationListener {
  final List<String> passed = new CopyOnWriteArrayList<>();
  final List<String> failed = new CopyOnWriteArrayList<>();
  final List<String> skipped = new CopyOnWriteArrayList<>();

  @Override
  public void onConfigurationSuccess(ITestResult tr) {
    passed.add(tr.getMethod().getMethodName());
  }

  @Override
  public void onConfigurationFailure(ITestResult tr) {
    failed.add(tr.getMethod().getMethodName());
  }

  @Override
  public void onConfigurationSkip(ITestResult tr) {
    skipped.add(tr.getMethod().getMethodName());
  }
}
