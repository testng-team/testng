package org.testng.internal;

import java.util.Map;
import org.testng.ITestNGMethod;

public class MethodArguments extends Arguments {

  protected final Object[] parameterValues;

  protected MethodArguments(
      Object instance, ITestNGMethod tm,
      Map<String, String> params, Object[] parameterValues) {
    super(instance, tm, params);
    this.parameterValues = parameterValues;
  }

  public Object[] getParameterValues() {
    return parameterValues;
  }

}
