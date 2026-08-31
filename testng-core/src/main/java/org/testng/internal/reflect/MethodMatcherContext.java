package org.testng.internal.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.jspecify.annotations.Nullable;
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
  private final @Nullable ITestResult testResult;
  private final ResolvedParameters resolvedParameters;

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
      final @Nullable ITestResult testResult) {
    this(method, arguments, testContext, testResult, ResolvedParameters.none());
  }

  /**
   * The same, for a method some of whose parameters an {@link org.testng.IParameterResolver} owns.
   *
   * @param method current method.
   * @param arguments user arguments.
   * @param testContext current test context.
   * @param testResult current test results.
   * @param resolvedParameters the parameters supplied by a resolver rather than by the arguments.
   */
  public MethodMatcherContext(
      final Method method,
      final Object[] arguments,
      final ITestContext testContext,
      final @Nullable ITestResult testResult,
      final ResolvedParameters resolvedParameters) {
    this.method = method;
    this.methodParameter = ReflectionRecipes.getMethodParameters(method);
    this.arguments = arguments;
    this.testContext = testContext;
    this.testResult = testResult;
    this.resolvedParameters = resolvedParameters;
  }

  public ResolvedParameters getResolvedParameters() {
    return resolvedParameters;
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

  public @Nullable ITestResult getTestResult() {
    return testResult;
  }
}
