package org.testng.parameters.samples.resolver;

import java.lang.reflect.Parameter;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * Throws on exactly its second resolution -- the retry of a first attempt -- and answers otherwise.
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
    if (resolutions.incrementAndGet() == 2) {
      throw new IllegalStateException("resolver broke on the retry");
    }
    return new CustomObject("first");
  }
}
