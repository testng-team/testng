package org.testng.internal.invokers;

import java.util.Map;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.internal.ConfigurationGroupMethods;

public class TestMethodArguments extends MethodArguments {

  private final ITestClass testClass;
  private final int parametersIndex;
  private final ITestNGMethod[] beforeMethods;
  private final ITestNGMethod[] afterMethods;
  private final ConfigurationGroupMethods groupMethods;

  private TestMethodArguments(
      Object instance,
      ITestNGMethod tm,
      Object[] parameterValues,
      int parametersIndex,
      Map<String, String> params,
      ITestClass testClass,
      ITestNGMethod[] beforeMethods,
      ITestNGMethod[] afterMethods,
      ConfigurationGroupMethods groupMethods) {
    super(instance, tm, params, parameterValues);
    this.parametersIndex = parametersIndex;
    this.beforeMethods = beforeMethods;
    this.afterMethods = afterMethods;
    this.groupMethods = groupMethods;
    this.testClass = testClass;
  }

  public int getParametersIndex() {
    return parametersIndex;
  }

  public ITestNGMethod[] getBeforeMethods() {
    return beforeMethods;
  }

  public ITestNGMethod[] getAfterMethods() {
    return afterMethods;
  }

  public ConfigurationGroupMethods getGroupMethods() {
    return groupMethods;
  }

  public ITestClass getTestClass() {
    return testClass;
  }

  public static class Builder {

    private Object instance;
    private ITestNGMethod tm;
    private Object[] parameterValues;
    private int parametersIndex;
    private Map<String, String> params;
    private ITestClass testClass;
    private ITestNGMethod[] beforeMethods;
    private ITestNGMethod[] afterMethods;
    private ConfigurationGroupMethods groupMethods;

    public Builder usingInstance(Object instance) {
      this.instance = instance;
      return this;
    }

    public Builder forTestMethod(ITestNGMethod tm) {
      this.tm = tm;
      return this;
    }

    public Builder withParameterValues(Object[] parameterValues) {
      this.parameterValues = parameterValues;
      return this;
    }

    public Builder withParametersIndex(int parametersIndex) {
      this.parametersIndex = parametersIndex;
      return this;
    }

    public Builder withParameters(Map<String, String> params) {
      this.params = params;
      return this;
    }

    public Builder forTestClass(ITestClass testClass) {
      this.testClass = testClass;
      return this;
    }

    public Builder usingBeforeMethods(ITestNGMethod[] beforeMethods) {
      this.beforeMethods = beforeMethods;
      return this;
    }

    public Builder usingAfterMethods(ITestNGMethod[] afterMethods) {
      this.afterMethods = afterMethods;
      return this;
    }

    public Builder usingGroupMethods(ConfigurationGroupMethods groupMethods) {
      this.groupMethods = groupMethods;
      return this;
    }

    public Builder usingArguments(TestMethodArguments attributes) {
      return usingInstance(attributes.getInstance())
          .forTestMethod(attributes.getTestMethod())
          .withParameterValues(attributes.getParameterValues())
          .withParametersIndex(attributes.getParametersIndex())
          .withParameters(attributes.getParameters())
          .forTestClass(attributes.getTestClass())
          .usingBeforeMethods(attributes.getBeforeMethods())
          .usingAfterMethods(attributes.getAfterMethods())
          .usingGroupMethods(attributes.getGroupMethods());
    }

    public TestMethodArguments build() {
      return new TestMethodArguments(
          instance,
          tm,
          parameterValues,
          parametersIndex,
          params,
          testClass,
          beforeMethods,
          afterMethods,
          groupMethods);
    }
  }
}
