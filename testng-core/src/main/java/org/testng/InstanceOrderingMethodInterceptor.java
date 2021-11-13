package org.testng;

import java.util.List;
import java.util.Map;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

/** A method interceptor that sorts its methods per instances (i.e. per class). */
class InstanceOrderingMethodInterceptor implements IMethodInterceptor {

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    return groupMethodsByInstance(methods);
  }

  /** The default method interceptor which sorts methods by instances (i.e. by class). */
  private List<IMethodInstance> groupMethodsByInstance(List<IMethodInstance> methods) {
    List<Object> instanceList = Lists.newArrayList();
    Map<Object, List<IMethodInstance>> map = Maps.newLinkedHashMap();
    for (IMethodInstance mi : methods) {
      Object instance = mi.getInstance();
      if (!instanceList.contains(instance)) {
        instanceList.add(instance);
      }
      List<IMethodInstance> l = map.computeIfAbsent(instance, k -> Lists.newArrayList());
      l.add(mi);
    }

    List<IMethodInstance> result = Lists.newArrayList();
    for (Object instance : instanceList) {
      result.addAll(map.get(instance));
    }

    return result;
  }
}
