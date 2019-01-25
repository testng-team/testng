package test.groups.issue182;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.collections.Maps;

public class LocalListener implements IInvokedMethodListener {

  private Map<String, List<String>> mapping = Maps.newHashMap();

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    String methodname = method.getTestMethod().getMethodName();
    mapping.put(methodname, Arrays.asList(method.getTestMethod().getGroups()));
  }

  public Map<String, List<String>> getMapping() {
    return mapping;
  }
}
