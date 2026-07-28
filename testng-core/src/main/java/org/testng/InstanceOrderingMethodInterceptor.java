package org.testng;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.internal.IInstanceIdentity;

/** A method interceptor that sorts its methods per instances (i.e. per class). */
class InstanceOrderingMethodInterceptor implements IMethodInterceptor {

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    return groupMethodsByInstance(methods);
  }

  /** The default method interceptor which sorts methods by instances (i.e. by class). */
  private List<IMethodInstance> groupMethodsByInstance(List<IMethodInstance> methods) {
    List<Object> instanceList = new ArrayList<>();
    Map<Object, List<IMethodInstance>> map = new LinkedHashMap<>();
    for (IMethodInstance mi : methods) {
      // Group by the per-instance id rather than the materialized instance so that ordering does
      // not force a lazy @Factory instance to be created before its test is due to run.
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
