package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.issue1339.BabyPanda;
import org.testng.internal.issue1339.LittlePanda;
import org.testng.internal.issue1456.TestClassSample;
import org.testng.internal.misamples.AbstractMoves;
import org.testng.internal.misamples.Batman;
import org.testng.internal.misamples.JohnTravoltaMoves;
import org.testng.internal.misamples.MickJagger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class ClassHelperTest {

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

  @Test
  public void testNoClassDefFoundError() {
    URLClassLoader urlClassLoader =
        new URLClassLoader(new URL[] {}) {
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
            throw new NoClassDefFoundError();
          }
        };
    ClassHelper.addClassLoader(urlClassLoader);
    String fakeClassName = UUID.randomUUID().toString();
    Assert.assertNull(
        ClassHelper.forName(fakeClassName),
        "The result should be null; no exception should be thrown.");
  }

  @Test(dataProvider = "data")
  public void testWithDefaultMethodsBeingOverridden(
      Class<?> cls, int expectedCount, String... expected) {
    Set<Method> methods = ClassHelper.getAvailableMethodsExcludingDefaults(cls);
    Assertions.assertThat(methods).hasSize(expectedCount);
    for (Method m : methods) {
      String actual = m.getDeclaringClass().getName() + "." + m.getName();
      Assertions.assertThat(expected).contains(actual);
    }
  }

  @DataProvider(name = "data")
  public Object[][] getTestData() {
    return new Object[][] {
      {MickJagger.class, 1, MickJagger.class.getName() + ".dance"},
      {
        JohnTravoltaMoves.class,
        2,
        new String[] {
          JohnTravoltaMoves.class.getName() + ".walk", AbstractMoves.class.getName() + ".dance"
        }
      },
      {
        Batman.class,
        3,
        new String[] {
          Batman.class.getName() + ".fly",
          Batman.class.getName() + ".liftWeights",
          Batman.class.getName() + ".yellSlogan"
        }
      }
    };
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
