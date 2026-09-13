package org.testng.parameters.samples.resolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/** Owns {@link FromResolver} parameters and answers {@code Object.hashCode()} for each of them. */
public class MethodAnsweringResolver implements IParameterResolver {

  public static final Method ANSWER;

  static {
    try {
      ANSWER = Object.class.getMethod("hashCode");
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return parameter.isAnnotationPresent(FromResolver.class);
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    return ANSWER;
  }
}
