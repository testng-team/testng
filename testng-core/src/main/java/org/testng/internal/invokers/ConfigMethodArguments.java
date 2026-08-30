package org.testng.internal.invokers;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

public class ConfigMethodArguments extends MethodArguments {

  private @Nullable IClass testClass;
  private final ITestNGMethod[] allMethods;
  private final XmlSuite suite;
  private final @Nullable ITestResult testMethodResult;
  private final boolean retriedTestMethod;

  private ConfigMethodArguments(
      @Nullable IClass testClass,
      @Nullable ITestNGMethod currentTestMethod,
      ITestNGMethod[] allMethods,
      XmlSuite suite,
      Map<String, String> params,
      Object @Nullable [] parameterValues,
      @Nullable Object instance,
      @Nullable ITestResult testMethodResult,
      boolean retriedTestMethod) {
    super(instance, currentTestMethod, params, parameterValues);
    this.testClass = testClass;
    this.allMethods = allMethods;
    this.suite = suite;
    this.testMethodResult = testMethodResult;
    this.retriedTestMethod = retriedTestMethod;
  }

  public @Nullable IClass getTestClass() {
    return testClass;
  }

  public ITestNGMethod[] getConfigMethods() {
    return allMethods;
  }

  public XmlSuite getSuite() {
    return suite;
  }

  public @Nullable ITestResult getTestMethodResult() {
    return testMethodResult;
  }

  /**
   * @return true if these configuration methods belong to an invocation that retries a failed test
   *     method. Such an invocation is not held back by the failures of the attempt it retries --
   *     see {@code TestInvoker.invokeMethod}, which runs the test method itself on the same terms.
   */
  public boolean isForRetriedTestMethod() {
    return retriedTestMethod;
  }

  public void setTestClass(IClass testClass) {
    this.testClass = testClass;
  }

  public static class Builder {

    private @Nullable IClass testClass;
    private @Nullable ITestNGMethod currentTestMethod;
    private ITestNGMethod @Nullable [] allMethods;
    private @Nullable XmlSuite suite;
    private @Nullable Map<String, String> params;
    private Object @Nullable [] parameterValues;
    private @Nullable Object instance;
    private @Nullable ITestResult testMethodResult;
    private boolean retriedTestMethod;

    public Builder forTestClass(IClass testClass) {
      this.testClass = testClass;
      return this;
    }

    public Builder forTestMethod(ITestNGMethod currentTestMethod) {
      this.currentTestMethod = currentTestMethod;
      return this;
    }

    public Builder usingConfigMethodsAs(ITestNGMethod[] allMethods) {
      if (allMethods == null) {
        allMethods = new ITestNGMethod[] {};
      }
      this.allMethods = allMethods;
      return this;
    }

    public Builder usingConfigMethodsAs(Collection<ITestNGMethod> allMethods) {
      return usingConfigMethodsAs(allMethods.toArray(new ITestNGMethod[0]));
    }

    public Builder forSuite(XmlSuite suite) {
      this.suite = suite;
      return this;
    }

    public Builder usingParameters(Map<String, String> params) {
      this.params = params;
      return this;
    }

    public Builder usingParameterValues(Object @Nullable [] parameterValues) {
      this.parameterValues = parameterValues;
      return this;
    }

    public Builder usingInstance(@Nullable Object instance) {
      this.instance = instance;
      return this;
    }

    public Builder withResult(ITestResult testMethodResult) {
      this.testMethodResult = testMethodResult;
      return this;
    }

    public Builder forRetriedTestMethod(boolean retriedTestMethod) {
      this.retriedTestMethod = retriedTestMethod;
      return this;
    }

    public ConfigMethodArguments build() {
      return new ConfigMethodArguments(
          testClass,
          currentTestMethod,
          Objects.requireNonNull(allMethods),
          Objects.requireNonNull(suite),
          Objects.requireNonNull(params),
          parameterValues,
          instance,
          testMethodResult,
          retriedTestMethod);
    }
  }
}
