package test.methodinterceptors.issue1263;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * Records what {@link IMethodInterceptor} is handed, and the dependencies each of those methods
 * carried at that moment. The names are taken eagerly because {@link
 * ITestNGMethod#upstreamDependencies()} answers a view of a set the runner writes to again once the
 * scheduling graph is built.
 */
public class RecordingInterceptor implements IMethodInterceptor {

  private final Map<String, Set<String>> upstream = new LinkedHashMap<>();
  private final Map<String, Set<String>> downstream = new LinkedHashMap<>();

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    for (IMethodInstance each : methods) {
      ITestNGMethod method = each.getMethod();
      upstream.put(method.getMethodName(), names(method.upstreamDependencies()));
      downstream.put(method.getMethodName(), names(method.downstreamDependencies()));
    }
    return methods;
  }

  public Set<String> received() {
    return upstream.keySet();
  }

  public Set<String> upstreamOf(String method) {
    return upstream.get(method);
  }

  public Set<String> downstreamOf(String method) {
    return downstream.get(method);
  }

  private static Set<String> names(Collection<ITestNGMethod> methods) {
    return methods.stream()
        .map(ITestNGMethod::getMethodName)
        .collect(Collectors.toCollection(TreeSet::new));
  }
}
