package org.testng.junit;

import org.testng.ITestMethodFinder;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.TestNGMethod;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.reflect.ReflectionHelper;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class locates all test and configuration methods according to JUnit. It is used to change
 * the strategy used by TestRunner to locate its test methods.
 *
 * @author Cedric Beust, May 3, 2004
 */
public class JUnitMethodFinder implements ITestMethodFinder {
  private final String m_testName;
  private final IAnnotationFinder m_annotationFinder;

  public JUnitMethodFinder(String testName, IAnnotationFinder finder) {
    m_testName = testName;
    m_annotationFinder = finder;
  }

  @Override
  public ITestNGMethod[] getTestMethods(Class cls, XmlTest xmlTest) {

    return privateFindTestMethods(
        method -> method.getName().startsWith("test") && method.getParameterTypes().length == 0,
        cls);
  }

  private ITestNGMethod[] privateFindTestMethods(INameFilter filter, Class cls) {
    List<ITestNGMethod> vResult = Lists.newArrayList();

    // We do not want to walk up the class hierarchy and accept the
    // same method twice (e.g. setUp) which would lead to double-invocation.
    // All relevant JUnit methods are parameter-less so we store accepted
    // method names in a Set to filter out duplicates.
    Set<String> acceptedMethodNames = new HashSet<>();

    //
    // Collect all methods that start with test
    //
    Class current = cls;
    while (!(current == Object.class)) {
      Method[] allMethods = ReflectionHelper.excludingMain(current);
      for (Method allMethod : allMethods) {
        ITestNGMethod m =
            new TestNGMethod(
                /* allMethods[i].getDeclaringClass(), */ allMethod,
                m_annotationFinder,
                null,
                null); /* @@@ */
        ConstructorOrMethod method = m.getConstructorOrMethod();
        String methodName = method.getName();
        if (filter.accept(method) && !acceptedMethodNames.contains(methodName)) {
          vResult.add(m);
          acceptedMethodNames.add(methodName);
        }
      }
      current = current.getSuperclass();
    }

    return vResult.toArray(new ITestNGMethod[0]);
  }

  @Override
  public ITestNGMethod[] getBeforeTestMethods(Class cls) {
    return privateFindTestMethods(method -> "setUp".equals(method.getName()), cls);
  }

  @Override
  public ITestNGMethod[] getAfterTestMethods(Class cls) {
    return privateFindTestMethods(method -> "tearDown".equals(method.getName()), cls);
  }

  @Override
  public ITestNGMethod[] getAfterClassMethods(Class cls) {
    return new ITestNGMethod[0];
  }

  @Override
  public ITestNGMethod[] getBeforeClassMethods(Class cls) {
    return new ITestNGMethod[0];
  }

  @Override
  public ITestNGMethod[] getBeforeSuiteMethods(Class cls) {
    return new ITestNGMethod[0];
  }

  @Override
  public ITestNGMethod[] getAfterSuiteMethods(Class cls) {
    return new ITestNGMethod[0];
  }

  @Override
  public ITestNGMethod[] getBeforeTestConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }

  @Override
  public ITestNGMethod[] getAfterTestConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }

  @Override
  public ITestNGMethod[] getBeforeGroupsConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }

  @Override
  public ITestNGMethod[] getAfterGroupsConfigurationMethods(Class testClass) {
    return new ITestNGMethod[0];
  }
}

/////////////

interface INameFilter {
  boolean accept(ConstructorOrMethod method);
}
