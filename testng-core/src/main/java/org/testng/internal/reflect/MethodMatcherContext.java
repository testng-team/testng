package org.testng.internal.reflect;

import java.lang.reflect.Method;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * Input context for MethodMatchers.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public class MethodMatcherContext {
  private final Method method;
  private final Parameter[] methodParameter;
  private final Object[] arguments;
  private final ITestContext testContext;
  private final ITestResult testResult;

  /**
   * Constructs a context for MethodMatchers.
   *
   * @param method current method.
   * @param arguments user arguments.
   * @param testContext current test context.
   * @param testResult current test results.
   */
  public MethodMatcherContext(
      final Method method,
      final Object[] arguments,
      final ITestContext testContext,
      final ITestResult testResult) {
    this.method = method;
    this.methodParameter = ReflectionRecipes.getMethodParameters(method);
    this.arguments = arguments;
    this.testContext = testContext;
    this.testResult = testResult;
  }

  public Parameter[] getMethodParameter() {
    return methodParameter;
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getArguments() {
    return arguments;
  }

  public ITestContext getTestContext() {
    return testContext;
  }

  public ITestResult getTestResult() {
    return testResult;
  }
}
