package org.testng.internal.invokers;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.ITestNGMethod;

public class MethodArguments extends Arguments {

  protected final Object @Nullable [] parameterValues;

  protected MethodArguments(
      @Nullable Object instance,
      @Nullable ITestNGMethod tm,
      Map<String, String> params,
      Object @Nullable [] parameterValues) {
    super(instance, tm, params);
    this.parameterValues = parameterValues;
  }

  public Object @Nullable [] getParameterValues() {
    return parameterValues;
  }
}
