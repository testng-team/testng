package test.commandline.issue341;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.collections.Maps;

import java.util.Collections;
import java.util.Set;

public class LocalLogAggregator implements IInvokedMethodListener {
  private static final Set<String> logs = Collections.newSetFromMap(Maps.newConcurrentMap());

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.addAll(Reporter.getOutput(testResult));
  }

  public static Set<String> getLogs() {
    return logs;
  }
}
