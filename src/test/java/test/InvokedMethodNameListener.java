package test;

import com.google.common.base.Joiner;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO replace other test IInvokedMethodListener by this one
public class InvokedMethodNameListener implements IInvokedMethodListener, ITestListener {

  private final Set<Object> testInstances = Sets.newHashSet();
  private final List<String> foundMethodNames = new ArrayList<>();
  private final List<String> invokedMethodNames = new ArrayList<>();
  private final List<String> failedMethodNames = new ArrayList<>();
  private final List<String> failedBeforeInvocationMethodNames = new ArrayList<>();
  private final List<String> skippedMethodNames = new ArrayList<>();
  private final List<String> skippedAfterInvocationMethodNames = new ArrayList<>();
  private final List<String> succeedMethodNames = new ArrayList<>();
  private final Map<String, ITestResult> results = new HashMap<>();
  private final Map<Class<?>, List<String>> mapping = Maps.newHashMap();
  private final boolean skipConfiguration;
  private final boolean wantSkippedMethodAfterInvocation;

  public InvokedMethodNameListener() {
    this(false);
  }

  public InvokedMethodNameListener(boolean skipConfiguration) {
    this(skipConfiguration, false);
  }

  public InvokedMethodNameListener(boolean skipConfiguration, boolean wantSkippedMethodAfterInvocation) {
    this.skipConfiguration = skipConfiguration;
    this.wantSkippedMethodAfterInvocation = wantSkippedMethodAfterInvocation;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (!(skipConfiguration && method.isConfigurationMethod())) {
      invokedMethodNames.add(getName(testResult));
    }
    testInstances.add(testResult.getInstance());
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    List<String> methodNames = mapping.get(testResult.getMethod().getRealClass());
    if (methodNames == null) {
      methodNames = Lists.newArrayList();
      mapping.put(testResult.getMethod().getRealClass(), methodNames);
    }
    methodNames.add(method.getTestMethod().getMethodName());
    String name = getName(testResult);
    switch (testResult.getStatus()) {
      case ITestResult.FAILURE:
        if (!(skipConfiguration && method.isConfigurationMethod())) {
          failedMethodNames.add(name);
        }
        break;
      case ITestResult.SKIP:
        if (!(skipConfiguration && method.isConfigurationMethod())) {
          if (wantSkippedMethodAfterInvocation) {
            skippedAfterInvocationMethodNames.add(name);
          } else {
            throw new IllegalStateException("A skipped test is not supposed to be invoked");
          }
        }
        break;
      case ITestResult.SUCCESS:
        if (!(skipConfiguration && method.isConfigurationMethod())) {
          succeedMethodNames.add(name);
        }
        break;
      default:
        throw new AssertionError("Unexpected value: " + testResult.getStatus());
    }
  }

  @Override
  public void onTestStart(ITestResult result) {
    foundMethodNames.add(getName(result));
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!succeedMethodNames.contains(name)) {
      throw new IllegalStateException("A succeed test is supposed to be invoked");
    }
  }

  @Override
  public void onTestFailure(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!failedMethodNames.contains(name)) {
      failedBeforeInvocationMethodNames.add(name);
    }
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!skippedAfterInvocationMethodNames.contains(name)) {
      skippedMethodNames.add(name);
    }
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    String name = getName(result);
    results.put(name, result);
    if (!succeedMethodNames.contains(name) || !failedMethodNames.contains(name)) {
      throw new IllegalStateException("A FailedButWithinSuccessPercentage test is supposed to be invoked");
    }
  }

  @Override
  public void onStart(ITestContext context) {
  }

  @Override
  public void onFinish(ITestContext context) {
  }

  public Set<Object> getTestInstances() {
    return testInstances;
  }

  private static String getName(ITestResult result) {
    String testName = result.getName();
    String methodName = result.getMethod().getConstructorOrMethod().getName();
    String name;
    if (testName.contains(methodName)) {
      name = methodName;
    } else {
      name = testName + "#" + methodName;
    }
    if (result.getParameters().length != 0) {
      name = name + "(" + Joiner.on(",").useForNull("null").join(getParameterNames(result.getParameters())) + ")";
    }
    return name;
  }

  private static List<String> getParameterNames(Object[] parameters) {
    List<String> result = new ArrayList<>(parameters.length);
    for (Object parameter : parameters) {
      if (parameter == null) {
        result.add("null");
      } else {
        if (parameter instanceof Object[]) {
          result.add("[" + Joiner.on(",").useForNull("null").join((Object[]) parameter) + "]");
        } else {
          result.add(parameter.toString());
        }
      }
    }
    return result;
  }

  public List<String> getInvokedMethodNames() {
    return Collections.unmodifiableList(invokedMethodNames);
  }

  public List<String> getFailedMethodNames() {
    return Collections.unmodifiableList(failedMethodNames);
  }

  public List<String> getSkippedMethodNames() {
    return Collections.unmodifiableList(skippedMethodNames);
  }

  public List<String> getSucceedMethodNames() {
    return new ArrayList<>(succeedMethodNames);
  }

  public List<String> getFailedBeforeInvocationMethodNames() {
    return Collections.unmodifiableList(failedBeforeInvocationMethodNames);
  }

  public List<String> getSkippedAfterInvocationMethodNames() {
    return Collections.unmodifiableList(skippedAfterInvocationMethodNames);
  }

  public ITestResult getResult(String name) {
    return results.get(name);
  }

  public List<String> getMethodsForTestClass(Class<?> testClass) {
    return mapping.get(testClass);
  }
}
