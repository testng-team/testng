package org.testng.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.testng.DependencyMap;
import org.testng.ITestNGMethod;
import org.testng.TestRunner;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

public final class DynamicGraphHelper {

  private DynamicGraphHelper() {
    // Utility class. Defeat instantiation.
  }

  /** The form for a {@code <test>} that holds no {@code @BeforeGroups} method. */
  public static DynamicGraph<ITestNGMethod> createDynamicGraph(
      ITestNGMethod[] methods, XmlTest xmlTest) {
    return createDynamicGraph(methods, xmlTest, Collections.emptyMap());
  }

  /**
   * @param beforeGroupsMethods - The {@code @BeforeGroups} methods of the {@code <test>}, keyed by
   *     the group they run before, as {@link ConfigurationGroupMethods} holds them.
   */
  public static DynamicGraph<ITestNGMethod> createDynamicGraph(
      ITestNGMethod[] methods,
      XmlTest xmlTest,
      Map<String, List<ITestNGMethod>> beforeGroupsMethods) {
    DynamicGraph<ITestNGMethod> result = new DynamicGraph<>();

    DependencyMap dependencyMap = new DependencyMap(methods);
    Map<String, Map<String, List<ITestNGMethod>>> inheritedDependencies =
        inheritedGroupDependencies(methods, beforeGroupsMethods);

    // Keep track of whether we have group dependencies. If we do, preserve-order needs
    // to be ignored since group dependencies create inter-class dependencies which can
    // end up creating cycles when combined with preserve-order.
    final AtomicBoolean hasDependencies = new AtomicBoolean(false);
    Arrays.stream(methods)
        .forEach(
            m -> {
              // Attempt at adding the method instance to our dynamic graph
              // Addition to the graph will fail only when the method is already present.
              // Presence of a method in the graph is determined by its hashCode.
              // Since addition of the method was a failure lets now try to add it once again by
              // wrapping it
              // in a wrapper object which is capable of fudging the original hashCode.
              boolean added = result.addNode(m);
              if (!added) {
                result.addNode(new WrappedTestNGMethod(m));
              }

              String[] dependentMethods = m.getMethodsDependedUpon();
              Arrays.stream(dependentMethods)
                  .parallel()
                  .forEach(
                      d -> {
                        ITestNGMethod dm = dependencyMap.getMethodDependingOn(d, m);
                        // A method depending on itself needs no guard here: Edges.addEdge drops a
                        // self edge by equals.
                        result.addEdge(TestRunner.PriorityWeight.dependsOnMethods.ordinal(), m, dm);
                      });

              String[] dependentGroups = m.getGroupsDependedUpon();
              if (dependentGroups.length != 0) {
                hasDependencies.set(true);
              }
              Arrays.stream(dependentGroups)
                  .parallel()
                  .forEach(
                      d -> {
                        List<ITestNGMethod> dg = dependencyMap.getMethodsThatBelongTo(d, m);
                        dg.parallelStream()
                            .forEach(
                                ddm ->
                                    result.addEdge(
                                        TestRunner.PriorityWeight.dependsOnGroups.ordinal(),
                                        m,
                                        ddm));
                      });

              if (!inheritedDependencies.isEmpty()) {
                for (String ownGroup : m.getGroups()) {
                  Map<String, List<ITestNGMethod>> inherited =
                      inheritedDependencies.getOrDefault(ownGroup, Collections.emptyMap());
                  for (Map.Entry<String, List<ITestNGMethod>> each : inherited.entrySet()) {
                    // Skip a group the method itself belongs to: making every member of a group
                    // depend on the others is a cycle, not a dependency. The membership is decided
                    // by the same expression that resolved the group, so a dependency written as a
                    // pattern excludes the method the same way a plain name does.
                    if (MethodGroupsHelper.belongsToGroup(m, each.getKey())) {
                      continue;
                    }
                    hasDependencies.set(true);
                    result.addEdges(
                        TestRunner.PriorityWeight.dependsOnGroups.ordinal(), m, each.getValue());
                  }
                }
              }
            });

    // Preserve order
    // Don't preserve the ordering if we're running in parallel, otherwise the suite will
    // create multiple threads but these threads will be created one after the other,
    // giving the impression of parallelism (multiple thread id's) while still running
    // sequentially.
    if (!hasDependencies.get()
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
    if (canGroupByInstances(xmlTest)) {
      ListMultiMap<ITestNGMethod, ITestNGMethod> instanceDependencies =
          createInstanceDependencies(methods);
      for (Map.Entry<ITestNGMethod, List<ITestNGMethod>> es : instanceDependencies.entrySet()) {
        result.addEdges(
            TestRunner.PriorityWeight.groupByInstance.ordinal(), es.getKey(), es.getValue());
      }
    }

    return result;
  }

  /**
   * A {@code @BeforeGroups} method is not a node of this graph -- it is pulled dynamically, right
   * before the first test method of a group it runs before, and {@code
   * MethodHelper.topologicalSort} leaves the group dependencies of a group configuration method
   * alone for that same reason. Its {@code dependsOnGroups} therefore has to be carried by the test
   * methods of the group it runs before, which is the whole of GITHUB-2804. {@code @AfterGroups} is
   * deliberately left out: it fires after the last method of its group, so the only edge that is
   * sound for it is the same all-of-A-after-all-of-Z one, which is stronger than that annotation
   * asks for and would reorder suites that pass today.
   *
   * <p>The group the configuration runs before is matched by name, which is how {@link
   * ConfigurationGroupMethods#getBeforeGroupMethodsForGroup(String[])} picks it at invocation time.
   *
   * @return for each group a {@code @BeforeGroups} runs before, the test methods of every group
   *     that configuration depends upon, keyed by the depended-upon group. A group holding no
   *     method in this {@code <test>} is left out, so it stays the no-op it has always been.
   */
  private static Map<String, Map<String, List<ITestNGMethod>>> inheritedGroupDependencies(
      ITestNGMethod[] methods, Map<String, List<ITestNGMethod>> beforeGroupsMethods) {
    if (beforeGroupsMethods.isEmpty()) {
      return Collections.emptyMap();
    }
    // Resolving a group name walks every method, so do it once per distinct name rather than once
    // per method that inherits the dependency. DependencyMap indexes the same thing but throws on a
    // group holding no method, where a @BeforeGroups naming one has always been a no-op.
    Map<String, List<ITestNGMethod>> resolved = new HashMap<>();
    Map<String, Map<String, List<ITestNGMethod>>> result = new HashMap<>();
    for (Map.Entry<String, List<ITestNGMethod>> each : beforeGroupsMethods.entrySet()) {
      Map<String, List<ITestNGMethod>> inherited = new LinkedHashMap<>();
      for (ITestNGMethod configMethod : each.getValue()) {
        for (String dependency : configMethod.getGroupsDependedUpon()) {
          List<ITestNGMethod> targets =
              resolved.computeIfAbsent(
                  dependency,
                  d -> Arrays.asList(MethodGroupsHelper.findMethodsThatBelongToGroup(methods, d)));
          if (!targets.isEmpty()) {
            inherited.put(dependency, targets);
          }
        }
      }
      if (!inherited.isEmpty()) {
        result.put(each.getKey(), inherited);
      }
    }
    return Collections.unmodifiableMap(result);
  }

  private static Comparator<XmlClass> classComparator() {
    return Comparator.comparingInt(XmlClass::getIndex);
  }

  private static boolean canGroupByInstances(XmlTest xmlTest) {
    return xmlTest.getGroupByInstances() && !xmlTest.getParallel().equals(ParallelMode.INSTANCES);
  }

  private static ListMultiMap<ITestNGMethod, ITestNGMethod> createClassDependencies(
      ITestNGMethod[] methods, XmlTest test) {
    Map<String, List<ITestNGMethod>> classes = new HashMap<>();
    // Note: use a List here to preserve the ordering but make sure
    // we don't add the same class twice
    List<XmlClass> sortedClasses = new ArrayList<>();

    ListMultiMap<String, ITestNGMethod> methodsFromClass = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      methodsFromClass.put(Utils.requireTestClassOf(m).getName(), m);
    }

    final List<XmlClass> classesWithMethods =
        test.getXmlClasses().stream()
            .filter(xmlClass -> methodsFromClass.keySet().contains(xmlClass.getName()))
            .collect(Collectors.toList());

    for (XmlClass c : classesWithMethods) {
      classes.put(c.getName(), new ArrayList<>());
      if (!sortedClasses.contains(c)) {
        sortedClasses.add(c);
      }
    }

    // Sort the classes based on their order of appearance in the XML
    sortedClasses.sort(classComparator());

    Map<String, Integer> indexedClasses1 = new HashMap<>();
    Map<Integer, String> indexedClasses2 = new HashMap<>();
    int i = 0;
    for (XmlClass c : sortedClasses) {
      indexedClasses1.put(c.getName(), i);
      indexedClasses2.put(i, c.getName());
      i++;
    }

    ListMultiMap<ITestNGMethod, ITestNGMethod> result = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      String name = Utils.requireTestClassOf(m).getName();
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
      // Key by the per-instance id rather than the instantiated instance so that building instance
      // level dependencies never forces a lazy @Factory instance to be created during collection.
      instanceMap.put(IInstanceIdentity.getInstanceId(m), m);
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
