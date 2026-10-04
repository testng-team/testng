package org.testng;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.internal.IInstanceIdentity;

/**
 * A method interceptor that puts the test methods of each test instance next to each other.
 *
 * <p>{@link TestRunner} uses it when {@code preserve-order} is off.
 */
class InstanceOrderingMethodInterceptor implements IMethodInterceptor {

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    return groupMethodsByInstance(methods);
  }

  /**
   * Orders the methods so that the methods of each test instance come together. The instances keep
   * the order in which they first appear. The methods of one instance keep their order too.
   */
  private List<IMethodInstance> groupMethodsByInstance(List<IMethodInstance> methods) {
    List<Object> instanceList = new ArrayList<>();
    Map<Object, List<IMethodInstance>> map = new LinkedHashMap<>();
    for (IMethodInstance mi : methods) {
      // Key by the instance id, not by the instance. Reading the instance would make a lazy
      // @Factory create it before its tests run.
      Object instance = IInstanceIdentity.getInstanceId(mi.getMethod());
      if (!instanceList.contains(instance)) {
        instanceList.add(instance);
      }
      List<IMethodInstance> l = map.computeIfAbsent(instance, k -> new ArrayList<>());
      l.add(mi);
    }

    List<IMethodInstance> result = new ArrayList<>();
    for (Object instance : instanceList) {
      result.addAll(map.get(instance));
    }

    return result;
  }
}
