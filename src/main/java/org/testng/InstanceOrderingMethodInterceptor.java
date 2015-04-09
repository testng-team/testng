package org.testng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A method interceptor that sorts its methods per instances (i.e. per class).
 *
 *
 */
class InstanceOrderingMethodInterceptor implements IMethodInterceptor {

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods,
      ITestContext context)  {
    return groupMethodsByInstance(methods);
  }

  /**
   * The default method interceptor which sorts methods by instances (i.e. by class).
   */
  private List<IMethodInstance> groupMethodsByInstance(List<IMethodInstance> methods) {
    List<Object> instanceList = new ArrayList<>();
    Map<Object, List<IMethodInstance>> map = new HashMap<>();
    for (IMethodInstance mi : methods) {
      Object[] methodInstances = mi.getInstances();
      for (Object instance : methodInstances) {
        if (!instanceList.contains(instance)) {
          instanceList.add(instance);
        }
        List<IMethodInstance> l = map.get(instance);
        if (l == null) {
          l = new ArrayList<>();
          map.put(instance, l);
        }
        l.add(mi);
      }
    }

    List<IMethodInstance> result = new ArrayList<>();
    for (Object instance : instanceList) {
      result.addAll(map.get(instance));
    }

    return result;
  }

}
