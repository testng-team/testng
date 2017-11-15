package org.testng.internal;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.thread.graph.IWorker;

import java.util.List;
import java.util.Map;

class InstanceBasedParallelParallelWorker extends AbstractParallelWorker {
    @Override
    public List<IWorker<ITestNGMethod>> createWorkers(Arguments arguments) {
        ListMultiMap<Object, ITestNGMethod> lmm = Maps.newSortedListMultiMap();
        for (ITestNGMethod m : arguments.getMethods()) {
            lmm.put(m.getInstance(), m);
        }
        List<IWorker<ITestNGMethod>> result = Lists.newArrayList();
        for (Map.Entry<Object, List<ITestNGMethod>> es : lmm.entrySet()) {
            List<IMethodInstance> methodInstances = MethodHelper.methodsToMethodInstances(es.getValue());
            TestMethodWorker tmw = new TestMethodWorker(arguments.getInvoker(),
                    methodInstances,
                    arguments.getTestContext().getCurrentXmlTest().getSuite(),
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
