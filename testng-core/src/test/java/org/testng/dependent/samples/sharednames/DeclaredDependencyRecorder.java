package org.testng.dependent.samples.sharednames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * Records what each method declared it depends on, before the run is scheduled.
 *
 * <p>An interceptor is the only place to read this. {@link ITestNGMethod#upstreamDependencies()}
 * answers the declared {@code dependsOnMethods} and {@code dependsOnGroups} here. Once the run is
 * scheduled it also answers the order that {@code preserve-order} and {@code group-by-instances}
 * impose, so a listener reading it later sees methods the author never named.
 */
public class DeclaredDependencyRecorder implements IMethodInterceptor {
  private final Map<String, Set<String>> declared = new HashMap<>();

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    for (IMethodInstance instance : methods) {
      ITestNGMethod method = instance.getMethod();
      declared.put(
          method.getQualifiedName(),
          method.upstreamDependencies().stream()
              .map(ITestNGMethod::getQualifiedName)
              .collect(Collectors.toSet()));
    }
    return methods;
  }

  /** Answers what the named method declared it depends on, or null when it was never seen. */
  public Set<String> declaredFor(String qualifiedName) {
    return declared.get(qualifiedName);
  }
}
