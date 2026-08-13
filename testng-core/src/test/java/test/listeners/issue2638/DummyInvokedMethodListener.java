package test.listeners.issue2638;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class DummyInvokedMethodListener implements IInvokedMethodListener {
  private static final Map<String, List<String>> methods = new HashMap<>();

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    String suiteName = testResult.getTestContext().getSuite().getName();
    methods
        .computeIfAbsent(suiteName, k -> new ArrayList<>())
        .add(method.getTestMethod().getQualifiedName());
  }

  public static List<String> getMethods(String suiteName) {
    return methods.getOrDefault(suiteName, Collections.emptyList());
  }

  public static void reset() {
    methods.clear();
  }
}
