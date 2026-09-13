package org.testng.parameters.samples.resolver;

import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * Answers the first invocation and throws on every later one -- a resolver that breaks on retry.
 */
public class FailsOnSecondResolutionResolver implements IParameterResolver {

  private final AtomicInteger resolutions = new AtomicInteger();

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return parameter.isAnnotationPresent(FromResolver.class);
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    if (resolutions.incrementAndGet() > 1) {
      throw new IllegalStateException("resolver broke on the retry");
    }
    return new CustomObject("first");
  }
}
