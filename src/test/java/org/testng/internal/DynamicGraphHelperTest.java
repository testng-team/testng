package org.testng.internal;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
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
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicGraphHelperTest extends SimpleBaseTest {

    @Test
    public void testCreateDynamicGraphAllIndependent() {
        DynamicGraph<ITestNGMethod> graph = newGraph(IndependentTestClassSample.class);
        assertThat(graph.getFreeNodes()).hasSize(2);
        for (Map.Entry<ITestNGMethod, List<DynamicGraph.Edge<ITestNGMethod>>> edge : graph.getEdges().entrySet()) {
            assertThat(edge.getValue()).isEmpty();
        }
    }

    @Test(dataProvider = "getDependencyData")
    public void testCreateDynamicGraphWithDependency(Class<?> clazz) {
        DynamicGraph<ITestNGMethod> graph = newGraph(clazz);
        assertThat(graph.getFreeNodes()).hasSize(1);
        List<DynamicGraph.Edge<ITestNGMethod>> edges = searchForMethod("b", graph);
        assertThat(edges).hasSize(1);
        edges = searchForMethod("a", graph);
        assertThat(edges).isEmpty();
    }

    @DataProvider(name = "getDependencyData")
    public Object[][] getDependencyData() {
        return new Object[][]{
                {SoftDependencyTestClassSample.class},
                {HardDependencyTestClassSample.class},
                {HardDependencyViaGroupsTestClassSample.class}
        };
    }

    @Test
    public void testCreateDynamicGraphWithPreserveOrderAndNoParallelism() {
        Class<?>[] classes = new Class<?>[]{SequentialClassA.class, SequentialClassB.class};
        XmlTest xmlTest = createXmlTest("suite", "test", classes);
        xmlTest.setPreserveOrder(true);
        xmlTest.setParallel(XmlSuite.ParallelMode.NONE);
        DynamicGraph<ITestNGMethod> graph = newGraph(xmlTest, classes);
        List<String> methodNames = Arrays.asList("testMethodOneSequentialClassB", "testMethodTwoSequentialClassB");
        for (String methodName : methodNames) {
            List<DynamicGraph.Edge<ITestNGMethod>> edges = searchForMethod(methodName, graph);
            assertThat(extractDestinationInfoFromEdge(edges))
                    .contains("testMethodOneSequentialClassA", "testMethodTwoSequentialClassA");
        }
    }

    @Test
    public void testCreateDynamicGraphWithGroupByInstances() {
        Class<?>[] classes = new Class<?>[]{FactoryTestClassSample.class};
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
        ITestNGMethod[] allMethods = methodList.toArray(new ITestNGMethod[methodList.size()]);
        for (Class<?> clazz : classes) {
            ITestClass testClass = new FakeTestClass(clazz);
            List<ITestNGMethod> tstMethods = Lists.newArrayList();
            MethodHelper.fixMethodsWithClass(allMethods, testClass, tstMethods);
        }

        DynamicGraph<ITestNGMethod> graph = DynamicGraphHelper.createDynamicGraph(allMethods, xmlTest);
        List<DynamicGraph.Edge<ITestNGMethod>> edges = searchForMethod("testMethod", graph, "two");
        Set<String> actualObjectIds = Sets.newHashSet();
        List<String> actualMethodNames = Lists.newLinkedList();
        for (DynamicGraph.Edge<ITestNGMethod> edge : edges) {
            actualObjectIds.add(edge.getTo().getInstance().toString());
            actualMethodNames.add(edge.getTo().getMethodName());
        }
        assertThat(actualObjectIds).containsExactly("one");
        assertThat(actualMethodNames).contains("testMethod", "anotherTestMethod");
    }

    private static List<String> extractDestinationInfoFromEdge(List<DynamicGraph.Edge<ITestNGMethod>> edges) {
        List<String> destinations = Lists.newLinkedList();
        for (DynamicGraph.Edge<ITestNGMethod> edge : edges) {
            destinations.add(edge.getTo().getMethodName());
        }
        return destinations;
    }

    private static List<DynamicGraph.Edge<ITestNGMethod>> searchForMethod(String methodName, DynamicGraph<ITestNGMethod> graph) {
        for (Map.Entry<ITestNGMethod, List<DynamicGraph.Edge<ITestNGMethod>>> edge : graph.getEdges().entrySet()) {
            if (edge.getKey().getMethodName().equals(methodName)) {
                return edge.getValue();
            }
        }
        return Lists.newLinkedList();
    }

    private static List<DynamicGraph.Edge<ITestNGMethod>> searchForMethod(String methodName, DynamicGraph<ITestNGMethod> graph, Object instance) {
        for (Map.Entry<ITestNGMethod, List<DynamicGraph.Edge<ITestNGMethod>>> edge : graph.getEdges().entrySet()) {
            if (edge.getKey().getMethodName().equals(methodName) && edge.getKey().getInstance().toString().equals(instance.toString())) {
                return edge.getValue();
            }
        }
        return Lists.newLinkedList();
    }

    private static DynamicGraph<ITestNGMethod> newGraph(Class<?>... classes) {
        XmlTest xmlTest = createXmlTest("suite", "test", classes);
        return newGraph(xmlTest, classes);
    }

    private static DynamicGraph<ITestNGMethod> newGraph(XmlTest xmlTest, Class<?>... classes) {
        ITestNGMethod[] methods = methods(ITestAnnotation.class, xmlTest, classes);
        return DynamicGraphHelper.createDynamicGraph(methods, xmlTest);
    }

    private static ITestNGMethod[] methods(Class<? extends IAnnotation> annotationClass, XmlTest xmlTest, Class<?>... classes) {
        List<ITestNGMethod> allMethods = Lists.newArrayList();
        for (Class<?> clazz : classes) {
            ITestClass testClass = new FakeTestClass(clazz);
            List<ITestNGMethod> tstMethods = Lists.newArrayList();
            MethodHelper.fixMethodsWithClass(methods(clazz, xmlTest, annotationClass), testClass, tstMethods);
            allMethods.addAll(tstMethods);
        }
        return allMethods.toArray(new ITestNGMethod[allMethods.size()]);
    }

    private static ITestNGMethod[] methods(Class<?> clazz, XmlTest xmlTest, Class<? extends IAnnotation> annotationClass) {
        IAnnotationFinder finder = new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
        return AnnotationHelper.findMethodsWithAnnotation(clazz, annotationClass, finder, xmlTest);
    }

}
