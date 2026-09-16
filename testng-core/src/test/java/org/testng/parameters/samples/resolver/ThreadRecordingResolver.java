package org.testng.parameters.samples.resolver;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.IParameterResolver;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/** Remembers which thread each of its answers was produced on. */
public class ThreadRecordingResolver implements IParameterResolver {

  private final Map<CustomObject, Thread> resolvedOn = new ConcurrentHashMap<>();

  @Override
  public boolean supportsParameter(
      Parameter parameter, ITestNGMethod method, ITestContext context) {
    return parameter.isAnnotationPresent(FromResolver.class);
  }

  @Override
  public Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context) {
    CustomObject answer = new CustomObject("on " + Thread.currentThread().getName());
    resolvedOn.put(answer, Thread.currentThread());
    return answer;
  }

  public Map<CustomObject, Thread> resolvedOn() {
    return resolvedOn;
  }
}
