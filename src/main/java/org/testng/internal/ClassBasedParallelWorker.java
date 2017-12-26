package org.testng.internal;

import org.testng.IMethodInstance;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ClassBasedParallelWorker extends AbstractParallelWorker {

    @Override
    public List<IWorker<ITestNGMethod>> createWorkers(Arguments arguments) {
        List<IWorker<ITestNGMethod>> result = Lists.newArrayList();
        // Methods that belong to classes with a sequential=true or parallel=classes
        // attribute must all be run in the same worker
        Set<Class<?>> sequentialClasses = Sets.newHashSet();
        for (ITestNGMethod m : arguments.getMethods()) {
            Class<? extends ITestClass> cls = m.getRealClass();
            org.testng.annotations.ITestAnnotation test =
                    arguments.getFinder().findAnnotation(cls, org.testng.annotations.ITestAnnotation.class);

            // If either sequential=true or parallel=classes, mark this class sequential
            if (isSequential(test, arguments.getTestContext().getCurrentXmlTest())) {
                sequentialClasses.add(cls);
            }
        }

        List<IMethodInstance> methodInstances = Lists.newArrayList();
        for (ITestNGMethod tm : arguments.getMethods()) {
            methodInstances.addAll(methodsToMultipleMethodInstances(tm));
        }

        Set<Class<?>> processedClasses = Sets.newHashSet();
        Map<String, String> params = null;
        Class<?> prevClass = null;
        for (IMethodInstance im : methodInstances) {
            Class<?> c = im.getMethod().getTestClass().getRealClass();
            if (!c.equals(prevClass)) {
                //Calculate the parameters to be injected only once per Class and NOT for every iteration.
                params = getParameters(im);
                prevClass = c;
            }
            if (sequentialClasses.contains(c)) {
                if (!processedClasses.contains(c)) {
                    processedClasses.add(c);
                    // Sequential class: all methods in one worker
                    TestMethodWorker worker = createTestMethodWorker(arguments, methodInstances, params, c);
                    result.add(worker);
                }
            }
            else {
                // Parallel class: each method in its own worker
                TestMethodWorker worker = createTestMethodWorker(arguments, Collections.singletonList(im), params, c);
                result.add(worker);
            }
        }

        return result;
    }

    private static List<IMethodInstance> findClasses(List<IMethodInstance> methodInstances, Class<?> c) {
        List<IMethodInstance> result = Lists.newArrayList();
        for (IMethodInstance mi : methodInstances) {
            if (mi.getMethod().getTestClass().getRealClass() == c) {
                result.add(mi);
            }
        }
        return result;
    }

    private static TestMethodWorker createTestMethodWorker(Arguments attributes,
            List<IMethodInstance> methodInstances, Map<String, String> params,
            Class<?> c) {
        return new TestMethodWorker(attributes.getInvoker(),
                findClasses(methodInstances, c),
                attributes.getTestContext().getCurrentXmlTest().getSuite(),
                params,
                attributes.getConfigMethods(),
                attributes.getClassMethodMap(),
                attributes.getTestContext(),
                attributes.getListeners());
    }

    private List<MethodInstance> methodsToMultipleMethodInstances(ITestNGMethod... methods) {
        List<MethodInstance> vResult = Lists.newArrayList();
        for (ITestNGMethod m : methods) {
            vResult.add(new MethodInstance(m));
        }

        return vResult;
    }

    private static boolean isSequential(org.testng.annotations.ITestAnnotation test, XmlTest xmlTest) {
        return test != null && (test.getSequential() || test.getSingleThreaded()) ||
                XmlSuite.ParallelMode.CLASSES.equals(xmlTest.getParallel());
    }

    private static Map<String, String> getParameters(IMethodInstance im) {
        XmlTest xmlTest = im.getMethod().getXmlTest();
        return im.getMethod().findMethodParameters(xmlTest);
    }

}
