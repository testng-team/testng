package test.commandline.issue341;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;

public class LocalLogAggregator implements IInvokedMethodListener {
  private static final Set<String> logs = Collections.newSetFromMap(new ConcurrentHashMap<>());

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.addAll(Reporter.getOutput(testResult));
  }

  /**
   * @return a snapshot, so that a caller cannot observe or mutate the live collection while a run
   *     is still writing to it.
   */
  public static Set<String> getLogs() {
    return new HashSet<>(logs);
  }

  /** Static state survives the JVM, so a test that asserts on a count has to start from empty. */
  public static void clearLogs() {
    logs.clear();
  }
}
