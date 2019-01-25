package org.testng.internal;

import org.testng.DependencyMap;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.TestRunner;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class DynamicGraphHelper {

  private DynamicGraphHelper() {
    // Utility class. Defeat instantiation.
  }

  public static DynamicGraph<ITestNGMethod> createDynamicGraph(
      ITestNGMethod[] methods, XmlTest xmlTest) {
    DynamicGraph<ITestNGMethod> result = new DynamicGraph<>();

    DependencyMap dependencyMap = new DependencyMap(methods);

    // Keep track of whether we have group dependencies. If we do, preserve-order needs
    // to be ignored since group dependencies create inter-class dependencies which can
    // end up creating cycles when combined with preserve-order.
    boolean hasDependencies = false;
    for (ITestNGMethod m : methods) {
      // Attempt at adding the method instance to our dynamic graph
      // Addition to the graph will fail only when the method is already present.
      // Presence of a method in the graph is determined by its hashCode.
      // Since addition of the method was a failure lets now try to add it once again by wrapping it
      // in a wrapper object which is capable of fudging the original hashCode.
      boolean added = result.addNode(m);
      if (!added) {
        result.addNode(new WrappedTestNGMethod(m));
      }

      String[] dependentMethods = m.getMethodsDependedUpon();
      if (dependentMethods != null) {
        for (String d : dependentMethods) {
          ITestNGMethod dm = dependencyMap.getMethodDependingOn(d, m);
          if (m != dm) {
            result.addEdge(TestRunner.PriorityWeight.dependsOnMethods.ordinal(), m, dm);
          }
        }
      }

      String[] dependentGroups = m.getGroupsDependedUpon();
      for (String d : dependentGroups) {
        hasDependencies = true;
        List<ITestNGMethod> dg = dependencyMap.getMethodsThatBelongTo(d, m);
        if (dg == null) {
          throw new TestNGException(
              "Method \"" + m + "\" depends on nonexistent group \"" + d + "\"");
        }
        for (ITestNGMethod ddm : dg) {
          result.addEdge(TestRunner.PriorityWeight.dependsOnGroups.ordinal(), m, ddm);
        }
      }
    }

    // Preserve order
    // Don't preserve the ordering if we're running in parallel, otherwise the suite will
    // create multiple threads but these threads will be created one after the other,
    // giving the impression of parallelism (multiple thread id's) while still running
    // sequentially.
    if (!hasDependencies
        && xmlTest.getParallel() == XmlSuite.ParallelMode.NONE
        && xmlTest.getPreserveOrder()) {
      // If preserve-order was specified and the class order is A, B
      // create a new set of dependencies where each method of B depends
      // on all the methods of A
      ListMultiMap<ITestNGMethod, ITestNGMethod> classDependencies =
          createClassDependencies(methods, xmlTest);

      for (Map.Entry<ITestNGMethod, List<ITestNGMethod>> es : classDependencies.entrySet()) {
        for (ITestNGMethod dm : es.getValue()) {
          result.addEdge(TestRunner.PriorityWeight.preserveOrder.ordinal(), dm, es.getKey());
        }
      }
    }

    // Group by instances
    if (xmlTest.getGroupByInstances()) {
      ListMultiMap<ITestNGMethod, ITestNGMethod> instanceDependencies =
          createInstanceDependencies(methods);
      for (Map.Entry<ITestNGMethod, List<ITestNGMethod>> es : instanceDependencies.entrySet()) {
        result.addEdges(
            TestRunner.PriorityWeight.groupByInstance.ordinal(), es.getKey(), es.getValue());
      }
    }

    return result;
  }

  private static Comparator<XmlClass> classComparator() {
    return Comparator.comparingInt(XmlClass::getIndex);
  }

  private static ListMultiMap<ITestNGMethod, ITestNGMethod> createClassDependencies(
      ITestNGMethod[] methods, XmlTest test) {
    Map<String, List<ITestNGMethod>> classes = Maps.newHashMap();
    // Note: use a List here to preserve the ordering but make sure
    // we don't add the same class twice
    List<XmlClass> sortedClasses = Lists.newArrayList();

    for (XmlClass c : test.getXmlClasses()) {
      classes.put(c.getName(), new ArrayList<>());
      if (!sortedClasses.contains(c)) {
        sortedClasses.add(c);
      }
    }

    // Sort the classes based on their order of appearance in the XML
    sortedClasses.sort(classComparator());

    Map<String, Integer> indexedClasses1 = Maps.newHashMap();
    Map<Integer, String> indexedClasses2 = Maps.newHashMap();
    int i = 0;
    for (XmlClass c : sortedClasses) {
      indexedClasses1.put(c.getName(), i);
      indexedClasses2.put(i, c.getName());
      i++;
    }

    ListMultiMap<String, ITestNGMethod> methodsFromClass = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      methodsFromClass.put(m.getTestClass().getName(), m);
    }

    ListMultiMap<ITestNGMethod, ITestNGMethod> result = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      String name = m.getTestClass().getName();
      Integer index = indexedClasses1.get(name);
      // The index could be null if the classes listed in the XML are different
      // from the methods being run (e.g. the .xml only contains a factory that
      // instantiates methods from a different class). In this case, we cannot
      // perform any ordering.
      if (index != null && index > 0) {
        // Make this method depend on all the methods of the class in the previous
        // index
        String classDependedUpon = indexedClasses2.get(index - 1);
        List<ITestNGMethod> methodsDependedUpon = methodsFromClass.get(classDependedUpon);
        for (ITestNGMethod mdu : methodsDependedUpon) {
          result.put(mdu, m);
        }
      }
    }

    return result;
  }

  private static ListMultiMap<ITestNGMethod, ITestNGMethod> createInstanceDependencies(
      ITestNGMethod[] methods) {
    ListMultiMap<Object, ITestNGMethod> instanceMap = Maps.newSortedListMultiMap();
    for (ITestNGMethod m : methods) {
      instanceMap.put(m.getInstance(), m);
    }

    ListMultiMap<ITestNGMethod, ITestNGMethod> result = Maps.newListMultiMap();
    Object previousInstance = null;
    for (Map.Entry<Object, List<ITestNGMethod>> es : instanceMap.entrySet()) {
      if (previousInstance == null) {
        previousInstance = es.getKey();
      } else {
        List<ITestNGMethod> previousMethods = instanceMap.get(previousInstance);
        Object currentInstance = es.getKey();
        List<ITestNGMethod> currentMethods = instanceMap.get(currentInstance);
        // Make all the methods from the current instance depend on the methods of
        // the previous instance
        for (ITestNGMethod cm : currentMethods) {
          for (ITestNGMethod pm : previousMethods) {
            result.put(cm, pm);
          }
        }
        previousInstance = currentInstance;
      }
    }

    return result;
  }
}
