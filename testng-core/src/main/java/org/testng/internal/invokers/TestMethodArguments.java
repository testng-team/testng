package org.testng.internal.invokers;

import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
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
      @Nullable Object instance,
      @Nullable ITestNGMethod tm,
      Object @Nullable [] parameterValues,
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

  /**
   * The inherited getter is nullable only to serve {@link ConfigMethodArguments}, which stands for
   * suite and test level configurations that have no current test method. A test method invocation
   * always has one, so this narrows the contract back.
   */
  @Override
  public ITestNGMethod getTestMethod() {
    return Objects.requireNonNull(super.getTestMethod());
  }

  /** Always present, for the same reason as {@link #getTestMethod()}. */
  @Override
  public Object getInstance() {
    return Objects.requireNonNull(super.getInstance());
  }

  public static class Builder {

    private @Nullable Object instance;
    private @Nullable ITestNGMethod tm;
    private Object @Nullable [] parameterValues;
    private int parametersIndex;
    private @Nullable Map<String, String> params;
    private @Nullable ITestClass testClass;
    private ITestNGMethod @Nullable [] beforeMethods;
    private ITestNGMethod @Nullable [] afterMethods;
    private @Nullable ConfigurationGroupMethods groupMethods;

    public Builder usingInstance(@Nullable Object instance) {
      this.instance = instance;
      return this;
    }

    public Builder forTestMethod(ITestNGMethod tm) {
      this.tm = tm;
      return this;
    }

    public Builder withParameterValues(Object @Nullable [] parameterValues) {
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
          Objects.requireNonNull(params),
          Objects.requireNonNull(testClass),
          Objects.requireNonNull(beforeMethods),
          Objects.requireNonNull(afterMethods),
          Objects.requireNonNull(groupMethods));
    }
  }
}
