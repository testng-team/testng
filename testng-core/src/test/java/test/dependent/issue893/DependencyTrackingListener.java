package test.dependent.issue893;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Sets;

public class DependencyTrackingListener implements ITestListener {
  private final Map<String, Set<String>> downstreamDependencies = new HashMap<>();
  private final Map<String, Set<String>> upstreamDependencies = new HashMap<>();

  @Override
  public void onTestStart(ITestResult result) {
    ITestContext context = result.getTestContext();
    for (ITestNGMethod method : context.getAllTestMethods()) {
      String key = method.getQualifiedName();
      downstreamDependencies
          .computeIfAbsent(key, k -> Sets.newHashSet())
          .addAll(
              method.downstreamDependencies().stream()
                  .map(ITestNGMethod::getQualifiedName)
                  .collect(Collectors.toList()));
      upstreamDependencies
          .computeIfAbsent(key, k -> Sets.newHashSet())
          .addAll(
              method.upstreamDependencies().stream()
                  .map(ITestNGMethod::getQualifiedName)
                  .collect(Collectors.toList()));
    }
  }

  public Map<String, Set<String>> getUpstreamDependencies() {
    return upstreamDependencies;
  }

  public Map<String, Set<String>> getDownstreamDependencies() {
    return downstreamDependencies;
  }
}
