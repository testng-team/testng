package org.testng.internal.invokers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;
import org.testng.internal.IInstanceIdentity;
import org.testng.internal.MethodHelper;
import org.testng.thread.IWorker;

class InstanceBasedParallelParallelWorker extends AbstractParallelWorker {
  @Override
  public List<IWorker<ITestNGMethod>> createWorkers(Arguments arguments) {
    ListMultiMap<Object, ITestNGMethod> lmm = Maps.newSortedListMultiMap();
    for (ITestNGMethod m : arguments.getMethods()) {
      // Group by the per-instance id rather than the instantiated instance so that a lazy @Factory
      // instance is not created up-front merely to partition the methods into per-instance workers.
      lmm.put(IInstanceIdentity.getInstanceId(m), m);
    }
    List<IWorker<ITestNGMethod>> result = new ArrayList<>();
    IInvoker invoker = arguments.getInvoker();
    ITestInvoker testInvoker = invoker.getTestInvoker();
    IConfigInvoker configInvoker = invoker.getConfigInvoker();
    for (Map.Entry<Object, List<ITestNGMethod>> es : lmm.entrySet()) {
      List<IMethodInstance> methodInstances = MethodHelper.methodsToMethodInstances(es.getValue());
      TestMethodWorker tmw =
          new TestMethodWorker(
              testInvoker,
              configInvoker,
              methodInstances,
              arguments.getTestContext().getCurrentXmlTest().getAllParameters(),
              arguments.getConfigMethods(),
              arguments.getClassMethodMap(),
              arguments.getTestContext(),
              arguments.getListeners());
      result.add(tmw);
    }

    return result;
  }
}
