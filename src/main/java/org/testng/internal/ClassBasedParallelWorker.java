package org.testng.internal;

import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class ClassBasedParallelWorker extends AbstractParallelWorker {

  @Override
  public List<IWorker<ITestNGMethod>> createWorkers(Arguments arguments) {
    List<IWorker<ITestNGMethod>> result = Lists.newArrayList();
    // Methods that belong to classes with a sequential=true or parallel=classes
    // attribute must all be run in the same worker
    Set<Class<?>> sequentialClasses = Sets.newHashSet();
    for (ITestNGMethod m : arguments.getMethods()) {
      Class<?> cls = m.getRealClass();
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
        // Calculate the parameters to be injected only once per Class and NOT for every iteration.
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
      } else {
        // Parallel class: each method in its own worker
        TestMethodWorker worker =
            createTestMethodWorker(arguments, Collections.singletonList(im), params, c);
        result.add(worker);
      }
    }

    return result;
  }

  private static List<IMethodInstance> findClasses(
      List<IMethodInstance> methodInstances, Class<?> c) {
    return methodInstances
        .stream()
        .filter(mi -> mi.getMethod().getTestClass().getRealClass() == c)
        .collect(Collectors.toList());
  }

  private static TestMethodWorker createTestMethodWorker(
      Arguments attributes,
      List<IMethodInstance> methodInstances,
      Map<String, String> params,
      Class<?> c) {
    IInvoker invoker = attributes.getInvoker();
    ITestInvoker testInvoker = invoker.getTestInvoker();
    IConfigInvoker configInvoker = invoker.getConfigInvoker();
    return new TestMethodWorker(
        testInvoker, configInvoker, findClasses(methodInstances, c),
        params,
        attributes.getConfigMethods(),
        attributes.getClassMethodMap(),
        attributes.getTestContext(),
        attributes.getListeners());
  }

  private List<MethodInstance> methodsToMultipleMethodInstances(ITestNGMethod... methods) {
    return Arrays.stream(methods).map(MethodInstance::new).collect(Collectors.toList());
  }

  private static boolean isSequential(
      org.testng.annotations.ITestAnnotation test, XmlTest xmlTest) {
    return test != null && test.getSingleThreaded()
        || XmlSuite.ParallelMode.CLASSES.equals(xmlTest.getParallel());
  }

  private static Map<String, String> getParameters(IMethodInstance im) {
    XmlTest xmlTest = im.getMethod().getXmlTest();
    return im.getMethod().findMethodParameters(xmlTest);
  }
}
