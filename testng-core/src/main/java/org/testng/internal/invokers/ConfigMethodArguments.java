package org.testng.internal.invokers;

import java.util.Collection;
import java.util.Map;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

public class ConfigMethodArguments extends MethodArguments {

  private IClass testClass;
  private final ITestNGMethod[] allMethods;
  private final XmlSuite suite;
  private final ITestResult testMethodResult;

  private ConfigMethodArguments(
      IClass testClass,
      ITestNGMethod currentTestMethod,
      ITestNGMethod[] allMethods,
      XmlSuite suite,
      Map<String, String> params,
      Object[] parameterValues,
      Object instance,
      ITestResult testMethodResult) {
    super(instance, currentTestMethod, params, parameterValues);
    this.testClass = testClass;
    this.allMethods = allMethods;
    this.suite = suite;
    this.testMethodResult = testMethodResult;
  }

  public IClass getTestClass() {
    return testClass;
  }

  public ITestNGMethod[] getConfigMethods() {
    return allMethods;
  }

  public XmlSuite getSuite() {
    return suite;
  }

  public Map<String, String> getParameters() {
    return params;
  }

  public Object[] getParameterValues() {
    return parameterValues;
  }

  public Object getInstance() {
    return instance;
  }

  public ITestResult getTestMethodResult() {
    return testMethodResult;
  }

  public void setTestClass(IClass testClass) {
    this.testClass = testClass;
  }

  public static class Builder {

    private IClass testClass;
    private ITestNGMethod currentTestMethod;
    private ITestNGMethod[] allMethods;
    private XmlSuite suite;
    private Map<String, String> params;
    private Object[] parameterValues;
    private Object instance;
    private ITestResult testMethodResult;

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

    public Builder usingParameterValues(Object[] parameterValues) {
      this.parameterValues = parameterValues;
      return this;
    }

    public Builder usingInstance(Object instance) {
      this.instance = instance;
      return this;
    }

    public Builder withResult(ITestResult testMethodResult) {
      this.testMethodResult = testMethodResult;
      return this;
    }

    public ConfigMethodArguments build() {
      return new ConfigMethodArguments(
          testClass,
          currentTestMethod,
          allMethods,
          suite,
          params,
          parameterValues,
          instance,
          testMethodResult);
    }
  }
}
