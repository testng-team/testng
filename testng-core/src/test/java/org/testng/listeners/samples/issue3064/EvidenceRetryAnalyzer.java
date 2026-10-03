package org.testng.listeners.samples.issue3064;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class EvidenceRetryAnalyzer implements IRetryAnalyzer {

  public EvidenceRetryAnalyzer() {
    throw new RuntimeException("Failed on purpose");
  }

  @Override
  public boolean retry(ITestResult result) {
    return false;
  }
}
