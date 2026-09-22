package org.testng.configuration.samples;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.MethodHelper;
import org.testng.internal.MethodSorting;
import org.testng.internal.RunInfo;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Sorts configuration methods the way a TestNG run does, but from a starting order that the caller
 * picks.
 *
 * <p>A run finds configuration methods through a hash set. The hash includes the class and method
 * names, so a rename or a move changes the starting order. A test that only runs a sample can then
 * pass without the fix it was written for. A test that also sorts from every starting order keeps
 * that check.
 */
public final class ConfigurationOrder {

  private ConfigurationOrder() {}

  /** Returns every order of the given names. */
  public static List<List<String>> allOrdersOf(List<String> names) {
    if (names.isEmpty()) {
      return List.of(List.of());
    }
    List<List<String>> result = new ArrayList<>();
    for (String first : names) {
      List<String> rest = new ArrayList<>(names);
      rest.remove(first);
      for (List<String> tail : allOrdersOf(rest)) {
        List<String> order = new ArrayList<>();
        order.add(first);
        order.addAll(tail);
        result.add(order);
      }
    }
    return result;
  }

  /**
   * Returns the names of the configuration methods of {@code testClass}, sorted the way a run sorts
   * them when it finds them in {@code startingOrder}. The methods must all be of one kind, for
   * example all {@code @BeforeMethod}, because a run sorts each kind on its own. The ordering comes
   * from {@code -Dtestng.order}, as in a run.
   */
  public static List<String> sortedFrom(Class<?> testClass, List<String> startingOrder)
      throws NoSuchMethodException {
    XmlTest xmlTest = new XmlTest(new XmlSuite());
    IAnnotationFinder finder = new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
    List<ITestNGMethod> methods = new ArrayList<>();
    for (String name : startingOrder) {
      methods.add(configurationMethod(testClass.getMethod(name), finder, xmlTest));
    }
    ITestNGMethod[] sorted =
        MethodHelper.collectAndOrderMethods(
            methods,
            false,
            runInfoThatIncludesEveryMethod(xmlTest),
            finder,
            false,
            new ArrayList<>(),
            MethodSorting.basedOn());
    return Arrays.stream(sorted).map(ITestNGMethod::getMethodName).collect(Collectors.toList());
  }

  private static ITestNGMethod configurationMethod(
      Method method, IAnnotationFinder finder, XmlTest xmlTest) {
    return new ConfigurationMethod(
        new ITestObjectFactory() {},
        new ConstructorOrMethod(method),
        finder,
        method.isAnnotationPresent(BeforeSuite.class),
        method.isAnnotationPresent(AfterSuite.class),
        method.isAnnotationPresent(BeforeTest.class),
        method.isAnnotationPresent(AfterTest.class),
        method.isAnnotationPresent(BeforeClass.class),
        method.isAnnotationPresent(AfterClass.class),
        method.isAnnotationPresent(BeforeMethod.class),
        method.isAnnotationPresent(AfterMethod.class),
        false,
        new String[0],
        new String[0],
        xmlTest,
        null);
  }

  /**
   * A run adds the method selector of the suite file. This check is about order, not selection, so
   * it keeps every method.
   */
  private static RunInfo runInfoThatIncludesEveryMethod(XmlTest xmlTest) {
    RunInfo runInfo = new RunInfo(() -> xmlTest);
    runInfo.addMethodSelector(
        new IMethodSelector() {
          @Override
          public boolean includeMethod(
              IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod) {
            return true;
          }

          @Override
          public void setTestMethods(List<ITestNGMethod> testMethods) {}
        },
        0);
    return runInfo;
  }
}
