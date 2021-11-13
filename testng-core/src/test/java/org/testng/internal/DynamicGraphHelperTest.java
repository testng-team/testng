package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.dynamicgraph.FactoryTestClassSample;
import org.testng.internal.dynamicgraph.FakeTestClass;
import org.testng.internal.dynamicgraph.FakeWrappedFactoryMethod;
import org.testng.internal.dynamicgraph.HardDependencyTestClassSample;
import org.testng.internal.dynamicgraph.HardDependencyViaGroupsTestClassSample;
import org.testng.internal.dynamicgraph.IndependentTestClassSample;
import org.testng.internal.dynamicgraph.SequentialClassA;
import org.testng.internal.dynamicgraph.SequentialClassB;
import org.testng.internal.dynamicgraph.SoftDependencyTestClassSample;
import org.testng.internal.dynamicgraph.testpackage.PackageTestClassA;
import org.testng.internal.dynamicgraph.testpackage.PackageTestClassBAbstract;
import org.testng.internal.dynamicgraph.testpackage.PackageTestClassBB;
import org.testng.internal.objects.InstanceCreator;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class DynamicGraphHelperTest extends SimpleBaseTest {

  private static final IAnnotationFinder finder =
      new JDK15AnnotationFinder(new DefaultAnnotationTransformer());

  @Test
  public void testCreateDynamicGraphAllIndependent() {
    DynamicGraph<ITestNGMethod> graph = newGraph(IndependentTestClassSample.class);
    assertThat(graph.getFreeNodes()).hasSize(2);
    assertThat(graph.getEdges()).isEmpty();
  }

  @Test(dataProvider = "getDependencyData")
  public void testCreateDynamicGraphWithDependency(Class<?> clazz) {
    DynamicGraph<ITestNGMethod> graph = newGraph(clazz);
    assertThat(graph.getFreeNodes()).hasSize(1);
    Map<ITestNGMethod, Integer> edges = searchForMethod("b", graph);
    assertThat(edges).hasSize(1);
    edges = searchForMethod("a", graph);
    assertThat(edges).isEmpty();
  }

  @Test(dataProvider = "getSoftDependencyData")
  public void testCreateDynamicGraphWithSoftDependency(Class<?> clazz) {
    DynamicGraph<ITestNGMethod> graph = newGraph(clazz);
    assertThat(graph.getFreeNodes()).hasSize(2);
    Map<ITestNGMethod, Integer> edges = searchForMethod("b", graph);
    assertThat(edges).isEmpty();
    edges = searchForMethod("a", graph);
    assertThat(edges).isEmpty();
  }

  @DataProvider(name = "getDependencyData")
  public Object[][] getDependencyData() {
    return new Object[][] {
      {HardDependencyTestClassSample.class}, {HardDependencyViaGroupsTestClassSample.class}
    };
  }

  @DataProvider(name = "getSoftDependencyData")
  public Object[][] getSoftDependencyData() {
    return new Object[][] {{SoftDependencyTestClassSample.class}};
  }

  @Test
  public void testCreateDynamicGraphWithPreserveOrderAndNoParallelism() {
    Class<?>[] classes = new Class<?>[] {SequentialClassA.class, SequentialClassB.class};
    XmlTest xmlTest = createXmlTest("suite", "test", classes);
    xmlTest.setPreserveOrder(true);
    xmlTest.setParallel(XmlSuite.ParallelMode.NONE);
    DynamicGraph<ITestNGMethod> graph = newGraph(xmlTest, classes);
    List<String> methodNames =
        Arrays.asList("testMethodOneSequentialClassB", "testMethodTwoSequentialClassB");
    for (String methodName : methodNames) {
      Map<ITestNGMethod, Integer> edges = searchForMethod(methodName, graph);
      assertThat(extractDestinationInfoFromEdge(edges))
          .contains("testMethodOneSequentialClassA", "testMethodTwoSequentialClassA");
    }
  }

  @Test
  public void testCreateDynamicGraphWithGroupByInstances() {
    Class<?>[] classes = new Class<?>[] {FactoryTestClassSample.class};
    XmlTest xmlTest = createXmlTest("suite", "test", classes);
    xmlTest.setGroupByInstances(true);
    ITestNGMethod[] methods = methods(ITestAnnotation.class, xmlTest, classes);
    List<ITestNGMethod> methodList = Lists.newLinkedList();
    List<FactoryTestClassSample> objects = Lists.newLinkedList();
    objects.add(new FactoryTestClassSample("one"));
    objects.add(new FactoryTestClassSample("two"));
    for (FactoryTestClassSample object : objects) {
      for (ITestNGMethod method : methods) {
        methodList.add(new FakeWrappedFactoryMethod(method, object));
      }
    }
    ITestNGMethod[] allMethods = methodList.toArray(new ITestNGMethod[0]);
    for (Class<?> clazz : classes) {
      ITestClass testClass = new FakeTestClass(clazz);
      List<ITestNGMethod> tstMethods = Lists.newArrayList();
      MethodHelper.fixMethodsWithClass(allMethods, testClass, tstMethods);
    }

    DynamicGraph<ITestNGMethod> graph = DynamicGraphHelper.createDynamicGraph(allMethods, xmlTest);
    Map<ITestNGMethod, Integer> edges = searchForMethod("testMethod", graph, "two");
    Set<String> actualObjectIds = Sets.newHashSet();
    List<String> actualMethodNames = Lists.newLinkedList();
    for (ITestNGMethod to : edges.keySet()) {
      actualObjectIds.add(to.getInstance().toString());
      actualMethodNames.add(to.getMethodName());
    }
    assertThat(actualObjectIds).containsExactly("one");
    assertThat(actualMethodNames).contains("testMethod", "anotherTestMethod");
  }

  @DataProvider
  public Object[][] classesFromPackage() {
    return new Object[][] {
      {
        new Class<?>[] {
          PackageTestClassA.class, PackageTestClassBAbstract.class, PackageTestClassBB.class
        }
      },
      {new Class<?>[] {PackageTestClassA.class, PackageTestClassBB.class}}
    };
  }

  @Test(dataProvider = "classesFromPackage")
  public void testCreateDynamicGraphWithPackageWithAbstractClassPreserveOrderTrue(
      Class<?>[] classes) {
    XmlTest xmlTest = createXmlTest("2249_suite", "2249_test", classes);
    xmlTest.setPreserveOrder(true);
    DynamicGraph<ITestNGMethod> graph = newGraph(xmlTest, classes);
    assertThat(
            graph.getFreeNodes().stream()
                .map(ITestNGMethod::getMethodName)
                .collect(Collectors.toList()))
        .containsExactly("a1", "a2");
  }

  @Test(dataProvider = "classesFromPackage")
  public void testCreateDynamicGraphWithPackageWithoutAbstractClass(Class<?>[] classes) {
    XmlTest xmlTest = createXmlTest("2249_suite", "2249_test", classes);
    xmlTest.setPreserveOrder(false);
    DynamicGraph<ITestNGMethod> graph = newGraph(xmlTest, classes);
    assertThat(
            graph.getFreeNodes().stream()
                .map(ITestNGMethod::getMethodName)
                .collect(Collectors.toList()))
        .containsExactly("a1", "a2", "b2", "b1");
  }

  private static List<String> extractDestinationInfoFromEdge(Map<ITestNGMethod, Integer> edges) {
    List<String> destinations = Lists.newLinkedList();
    for (ITestNGMethod to : edges.keySet()) {
      destinations.add(to.getMethodName());
    }
    return destinations;
  }

  private static Map<ITestNGMethod, Integer> searchForMethod(
      String methodName, DynamicGraph<ITestNGMethod> graph) {
    return searchForMethod(methodName, graph, null);
  }

  private static Map<ITestNGMethod, Integer> searchForMethod(
      String methodName, DynamicGraph<ITestNGMethod> graph, Object instance) {
    for (Map.Entry<ITestNGMethod, Map<ITestNGMethod, Integer>> edge : graph.getEdges().entrySet()) {
      if (edge.getKey().getMethodName().equals(methodName)
          && (instance == null
              || edge.getKey().getInstance().toString().equals(instance.toString()))) {
        return edge.getValue();
      }
    }
    return Maps.newHashMap();
  }

  private static DynamicGraph<ITestNGMethod> newGraph(Class<?>... classes) {
    XmlTest xmlTest = createXmlTest("suite", "test", classes);
    return newGraph(xmlTest, classes);
  }

  private static DynamicGraph<ITestNGMethod> newGraph(XmlTest xmlTest, Class<?>... classes) {
    ITestNGMethod[] methods = methods(ITestAnnotation.class, xmlTest, classes);
    return DynamicGraphHelper.createDynamicGraph(methods, xmlTest);
  }

  private static ITestNGMethod[] methods(
      Class<? extends IAnnotation> annotationClass, XmlTest xmlTest, Class<?>... classes) {
    List<ITestNGMethod> allMethods = Lists.newArrayList();
    for (Class<?> clazz : classes) {
      List<ITestNGMethod> tstMethods = associateInstanceToMethods(clazz, xmlTest, annotationClass);
      allMethods.addAll(tstMethods);
    }
    return allMethods.toArray(new ITestNGMethod[0]);
  }

  private static List<ITestNGMethod> associateInstanceToMethods(
      Class<?> clazz, XmlTest xmlTest, Class<? extends IAnnotation> annotationClass) {
    ITestClass testClass = new FakeTestClass(clazz);
    ITestNGMethod[] rawMethods = methods(clazz, xmlTest, annotationClass);
    Object object = newInstance(clazz);
    List<ITestNGMethod> fixedMethods = Lists.newArrayList();
    if (object == null) {
      // Looks like there was a non default constructor on the class (maybe because its driven by a
      // factory)
      // So lets not try to associate the instance. We will use the method as is.
      fixedMethods.addAll(Arrays.asList(rawMethods));
    } else {
      for (ITestNGMethod each : rawMethods) {
        ITestNGMethod m =
            new TestNGMethod(
                new ITestObjectFactory() {},
                each.getConstructorOrMethod().getMethod(),
                finder,
                xmlTest,
                object);
        fixedMethods.add(m);
      }
    }
    List<ITestNGMethod> tstMethods = Lists.newArrayList();
    MethodHelper.fixMethodsWithClass(
        fixedMethods.toArray(new ITestNGMethod[0]), testClass, tstMethods);
    return tstMethods;
  }

  private static Object newInstance(Class<?> clazz) {
    try {
      return InstanceCreator.newInstance(clazz);
    } catch (Exception e) {
      return null;
    }
  }

  private static ITestNGMethod[] methods(
      Class<?> clazz, XmlTest xmlTest, Class<? extends IAnnotation> annotationClass) {
    return AnnotationHelper.findMethodsWithAnnotation(
        new ITestObjectFactory() {}, clazz, annotationClass, finder, xmlTest);
  }
}
