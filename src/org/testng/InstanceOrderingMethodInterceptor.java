package org.testng;

import java.util.List;
import java.util.Map;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

/**
 * A method interceptor that sorts its methods per instances (i.e. per class).
 * 
 *
 */
public class InstanceOrderingMethodInterceptor implements IMethodInterceptor {

  public List<IMethodInstance> intercept(List<IMethodInstance> methods,
      ITestContext context)  {
    return groupMethodsByInstance(methods);
//    Collections.sort(methods, MethodInstance.SORT_BY_CLASS);
//    return methods;
  }

  /**
   * The default method interceptor which sorts methods by instances (i.e. by class).
   */
  private List<IMethodInstance> groupMethodsByInstance(List<IMethodInstance> methods) {
    List<Object> instanceList = Lists.newArrayList();
    Map<Object, List<IMethodInstance>> map = Maps.newHashMap();
    for (IMethodInstance mi : methods) {
      Object[] methodInstances = mi.getInstances();
      for (Object instance : methodInstances) {
        if (!instanceList.contains(instance)) instanceList.add(instance);
        List<IMethodInstance> l = map.get(instance);
        if (l == null) {
          l = Lists.newArrayList();
          map.put(instance, l);
        }
        l.add(mi);
      }
    }

    List<IMethodInstance> result = Lists.newArrayList();
    for (Object instance : instanceList) {
      result.addAll(map.get(instance));
    }

    return result;
  }
  
}
