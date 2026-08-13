package test.groups.issue182;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class LocalListener implements IInvokedMethodListener {

  private final Map<String, List<String>> mapping = new HashMap<>();

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    String methodname = method.getTestMethod().getMethodName();
    mapping.put(methodname, Arrays.asList(method.getTestMethod().getGroups()));
  }

  public Map<String, List<String>> getMapping() {
    return mapping;
  }
}
