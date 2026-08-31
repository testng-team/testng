package test.inject.parameterresolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Records what each sample test method was actually invoked with. */
public final class ParameterRecorder {

  private static final Map<String, List<Object[]>> INVOCATIONS = new ConcurrentHashMap<>();

  private ParameterRecorder() {}

  public static void record(String methodName, Object... parameters) {
    INVOCATIONS
        .computeIfAbsent(methodName, unused -> Collections.synchronizedList(new ArrayList<>()))
        .add(parameters);
  }

  public static List<Object[]> invocationsOf(String methodName) {
    return INVOCATIONS.getOrDefault(methodName, Collections.emptyList());
  }

  public static Object[] onlyInvocationOf(String methodName) {
    List<Object[]> invocations = invocationsOf(methodName);
    if (invocations.size() != 1) {
      throw new IllegalStateException(
          "expected exactly one invocation of " + methodName + ", got " + invocations.size());
    }
    return invocations.get(0);
  }

  public static void clear() {
    INVOCATIONS.clear();
  }
}
