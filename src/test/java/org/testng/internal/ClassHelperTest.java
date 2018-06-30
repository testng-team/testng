package org.testng.internal;

import org.testng.Assert;
import org.testng.IClass;
import org.testng.IObjectFactory;
import org.testng.ITest;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.issue1339.BabyPanda;
import org.testng.internal.issue1339.LittlePanda;
import org.testng.internal.issue1456.TestClassSample;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassHelperTest {
  private static final String GITHUB_1456 = "GITHUB-1456";

  @Test(description = GITHUB_1456)
  public void testCreateInstance1WithOneArgStringParamForConstructor() {
    Class<?> declaringClass = TestClassSample.class;
    Map<Class<?>, IClass> classes = Maps.newHashMap();
    XmlTest xmlTest = new XmlTest(new XmlSuite());
    xmlTest.setName(GITHUB_1456);
    IAnnotationFinder finder = new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
    IObjectFactory objectFactory = new ObjectFactoryImpl();
    Object object =
        ClassHelper.createInstance1(declaringClass, classes, xmlTest, finder, objectFactory, false);
    Assert.assertTrue(object instanceof ITest);
    Assert.assertEquals(((ITest) object).getTestName(), GITHUB_1456);
  }

  @Test
  public void testGetAvailableMethods() {
    runTest(getExpected(), LittlePanda.class);
  }

  @Test
  public void testGetAvailableMethodsWhenOverrdingIsInvolved() {
    List<String> expected = getExpected("equals", "hashCode", "toString");
    runTest(expected, BabyPanda.class);
  }

  @Test
  public void testFindClassInSameTest() {
    runTest(TestClassSample.class, 1, TestClassSample.class);
  }

  @Test
  public void testFindClassesInSameTest() {
    runTest(TestClassSample.class, 2, TestClassSample.class, BabyPanda.class);
  }

  private static void runTest(Class<?> classToBeFound, int expectedCount, Class<?>... classes) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("xml_suite");
    newXmlTest("test1", xmlSuite, classes);
    newXmlTest("test2", xmlSuite, classes);
    newXmlTest("test3", xmlSuite, classes);
    XmlClass[] xmlClasses = ClassHelper.findClassesInSameTest(classToBeFound, xmlSuite);
    assertThat(xmlClasses.length).isEqualTo(expectedCount);
  }

  private static void newXmlTest(String testname, XmlSuite xmlSuite, Class<?>... clazz) {
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setName(testname);
    xmlTest.setXmlClasses(newXmlClass(clazz));
  }

  private static List<XmlClass> newXmlClass(Class<?>... classes) {
    List<XmlClass> xmlClasses = new ArrayList<>();
    for (Class<?> clazz : classes) {
      xmlClasses.add(new XmlClass(clazz));
    }
    return xmlClasses;
  }

  private static void runTest(List<String> expected, Class<?> whichClass) {
    Set<Method> methods = ClassHelper.getAvailableMethods(whichClass);
    // Intentionally not using assertEquals because when this test is executed via gradle an
    // additional method
    // called "jacocoInit()" is getting added, which does not get added when this test is executed
    // individually
    int size = expected.size();
    Assert.assertTrue(
        methods.size() >= size, "Number of methods found should have been atleast " + size);
    for (Method method : methods) {
      if ("$jacocoInit".equalsIgnoreCase(method.getName())) {
        continue;
      }
      Assert.assertTrue(expected.contains(method.getName()));
    }
  }

  private static List<String> getExpected(String... additionalMethods) {
    String[] defaultMethods = new String[] {"announcer", "announcer", "inheritable", "inheritable"};
    if (additionalMethods == null) {
      return Arrays.asList(defaultMethods);
    }
    List<String> expected = new ArrayList<>(Arrays.asList(defaultMethods));
    expected.addAll(Arrays.asList(additionalMethods));
    return expected;
  }
}
