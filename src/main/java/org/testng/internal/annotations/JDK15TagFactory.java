package org.testng.internal.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.IAnnotationTransformer;
import org.testng.TestNGException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Configuration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Factory;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IExpectedExceptionsAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.IObjectFactoryAnnotation;
import org.testng.annotations.IParametersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.internal.Utils;

/**
 * This class creates implementations of IAnnotations based on the JDK5
 * annotation that was found on the Java element.
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JDK15TagFactory {

  public <A extends IAnnotation> A createTag(Class<?> cls, Annotation a,
                                             Class<A> annotationClass, IAnnotationTransformer transformer) {
    IAnnotation result = null;

    if (a != null) {
      if (annotationClass == IConfigurationAnnotation.class) {
        result = createConfigurationTag(cls, a);
      }
      else if (annotationClass == IDataProviderAnnotation.class) {
        result = createDataProviderTag(a);
      }
      else if (annotationClass == IExpectedExceptionsAnnotation.class) {
        result = createExpectedExceptionsTag(a);
      }
      else if (annotationClass == IFactoryAnnotation.class) {
        result = createFactoryTag(cls, a);
      }
      else if (annotationClass == IParametersAnnotation.class) {
        result = createParametersTag(a);
      }
      else if (annotationClass == IObjectFactoryAnnotation.class) {
        result = createObjectFactoryTag(a);
      }
      else if (annotationClass == ITestAnnotation.class) {
        result = createTestTag(cls, a, transformer);
      }
      else if (annotationClass == IListenersAnnotation.class) {
        result = createListenersTag(cls, a, transformer);
      }
      else if (annotationClass == IBeforeSuite.class || annotationClass == IAfterSuite.class ||
          annotationClass == IBeforeTest.class || annotationClass == IAfterTest.class ||
          annotationClass == IBeforeGroups.class || annotationClass == IAfterGroups.class ||
          annotationClass == IBeforeClass.class || annotationClass == IAfterClass.class ||
          annotationClass == IBeforeMethod.class || annotationClass == IAfterMethod.class)
      {
        result = maybeCreateNewConfigurationTag(cls, a, annotationClass);
      }

      else {
        throw new TestNGException("Unknown annotation requested:" + annotationClass);
      }
    }

    //noinspection unchecked
    return (A) result;
  }

  private IAnnotation maybeCreateNewConfigurationTag(Class<?> cls, Annotation a,
      Class<?> annotationClass)
  {
    IAnnotation result = null;

    if (annotationClass == IBeforeSuite.class) {
      BeforeSuite bs = (BeforeSuite) a;
      result = createConfigurationTag(cls, a,
          true, false,
          false, false,
          new String[0], new String[0],
          false, false,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IAfterSuite.class) {
      AfterSuite bs = (AfterSuite) a;
      result = createConfigurationTag(cls, a,
          false, true,
          false, false,
          new String[0], new String[0],
          false, false,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IBeforeTest.class) {
      BeforeTest bs = (BeforeTest) a;
      result = createConfigurationTag(cls, a,
          false, false,
          true, false,
          new String[0], new String[0],
          false, false,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IAfterTest.class) {
      AfterTest bs = (AfterTest) a;
      result = createConfigurationTag(cls, a,
          false, false,
          false, true,
          new String[0], new String[0],
          false, false,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IBeforeGroups.class) {
      BeforeGroups bs = (BeforeGroups) a;
      final String[] groups= bs.value().length > 0 ? bs.value() : bs.groups();
      result = createConfigurationTag(cls, a,
          false, false,
          false, false,
          groups, new String[0],
          false, false,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IAfterGroups.class) {
      AfterGroups bs = (AfterGroups) a;
      final String[] groups= bs.value().length > 0 ? bs.value() : bs.groups();
      result = createConfigurationTag(cls, a,
          false, false,
          false, false,
          new String[0], groups,
          false, false,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IBeforeClass.class) {
      BeforeClass bs = (BeforeClass) a;
      result = createConfigurationTag(cls, a,
          false, false,
          false, false,
          new String[0], new String[0],
          true, false,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IAfterClass.class) {
      AfterClass bs = (AfterClass) a;
      result = createConfigurationTag(cls, a,
          false, false,
          false, false,
          new String[0], new String[0],
          false, true,
          false, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, false,
          bs.timeOut());
    }
    else if (annotationClass == IBeforeMethod.class) {
      BeforeMethod bs = (BeforeMethod) a;
      result = createConfigurationTag(cls, a,
          false, false,
          false, false,
          new String[0], new String[0],
          false, false,
          true, false,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          bs.firstTimeOnly(), false,
          bs.timeOut());
    }
    else if (annotationClass == IAfterMethod.class) {
      AfterMethod bs = (AfterMethod) a;
      result = createConfigurationTag(cls, a,
          false, false,
          false, false,
          new String[0], new String[0],
          false, false,
          false, true,
          bs.alwaysRun(),
          bs.dependsOnGroups(), bs.dependsOnMethods(),
          bs.description(), bs.enabled(), bs.groups(),
          bs.inheritGroups(), null,
          false, bs.lastTimeOnly(),
          bs.timeOut());
    }

    return result;
  }

  @SuppressWarnings({"deprecation"})
  private ConfigurationAnnotation createConfigurationTag(Class<?> cls, Annotation a) {
    ConfigurationAnnotation result = new ConfigurationAnnotation();
    Configuration c = (Configuration) a;
    result.setBeforeTestClass(c.beforeTestClass());
    result.setAfterTestClass(c.afterTestClass());
    result.setBeforeTestMethod(c.beforeTestMethod());
    result.setAfterTestMethod(c.afterTestMethod());
    result.setBeforeTest(c.beforeTest());
    result.setAfterTest(c.afterTest());
    result.setBeforeSuite(c.beforeSuite());
    result.setAfterSuite(c.afterSuite());
    result.setBeforeGroups(c.beforeGroups());
    result.setAfterGroups(c.afterGroups());
    result.setParameters(c.parameters());
    result.setEnabled(c.enabled());

    result.setGroups(join(c.groups(), findInheritedStringArray(cls, Test.class, "groups")));
    result.setDependsOnGroups(c.dependsOnGroups());
    result.setDependsOnMethods(c.dependsOnMethods());
    result.setAlwaysRun(c.alwaysRun());
    result.setInheritGroups(c.inheritGroups());
    result.setDescription(c.description());

    return result;
  }

  private IAnnotation createConfigurationTag(Class<?> cls, Annotation a,
      boolean beforeSuite, boolean afterSuite,
      boolean beforeTest, boolean afterTest,
      String[] beforeGroups, String[] afterGroups,
      boolean beforeClass, boolean afterClass,
      boolean beforeMethod, boolean afterMethod,
      boolean alwaysRun,
      String[] dependsOnGroups, String[] dependsOnMethods,
      String description, boolean enabled, String[] groups,
      boolean inheritGroups, String[] parameters,
      boolean firstTimeOnly, boolean lastTimeOnly,
      long timeOut)
  {
    ConfigurationAnnotation result = new ConfigurationAnnotation();
    result.setFakeConfiguration(true);
    result.setBeforeSuite(beforeSuite);
    result.setAfterSuite(afterSuite);
    result.setBeforeTest(beforeTest);
    result.setAfterTest(afterTest);
    result.setBeforeTestClass(beforeClass);
    result.setAfterTestClass(afterClass);
    result.setBeforeGroups(beforeGroups);
    result.setAfterGroups(afterGroups);
    result.setBeforeTestMethod(beforeMethod);
    result.setAfterTestMethod(afterMethod);

    result.setAlwaysRun(alwaysRun);
    result.setDependsOnGroups(dependsOnGroups);
    result.setDependsOnMethods(dependsOnMethods);
    result.setDescription(description);
    result.setEnabled(enabled);
    result.setGroups(groups);
    result.setInheritGroups(inheritGroups);
    result.setParameters(parameters);
    result.setFirstTimeOnly(firstTimeOnly);
    result.setLastTimeOnly(lastTimeOnly);
    result.setTimeOut(timeOut);

    return result;
  }

  private IAnnotation createDataProviderTag(Annotation a) {
    DataProviderAnnotation result = new DataProviderAnnotation();
    DataProvider c = (DataProvider) a;
    result.setName(c.name());
    result.setParallel(c.parallel());

    return result;
  }

  @SuppressWarnings({"deprecation"})
  private IAnnotation createExpectedExceptionsTag(Annotation a) {
    ExpectedExceptionsAnnotation result = new ExpectedExceptionsAnnotation ();
    ExpectedExceptions c = (ExpectedExceptions ) a;
    result.setValue(c.value());

    return result;
  }

  @SuppressWarnings({"deprecation"})
  private IAnnotation createFactoryTag(Class<?> cls, Annotation a) {
    FactoryAnnotation result = new FactoryAnnotation();
    Factory c = (Factory) a;
    result.setParameters(c.parameters());
    result.setDataProvider(c.dataProvider());
    result.setDataProviderClass(
        findInherited(c.dataProviderClass(), cls, Factory.class, "dataProviderClass",
            DEFAULT_CLASS));
    result.setEnabled(c.enabled());

    return result;
  }

  private IAnnotation createObjectFactoryTag(Annotation a) {
    return new ObjectFactoryAnnotation();
  }

  private IAnnotation createParametersTag(Annotation a) {
    ParametersAnnotation result = new ParametersAnnotation();
    Parameters c = (Parameters) a;
    result.setValue(c.value());

    return result;
  }

  @SuppressWarnings({"deprecation"})
  private IAnnotation createListenersTag(Class<?> cls, Annotation a,
      IAnnotationTransformer transformer)
  {
    ListenersAnnotation result = new ListenersAnnotation();
    Listeners l = (Listeners) a;
    result.setValue(l.value());

    return result;
  }

  @SuppressWarnings({"deprecation"})
  private IAnnotation createTestTag(Class<?> cls, Annotation a,
      IAnnotationTransformer transformer)
  {
    TestAnnotation result = new TestAnnotation();
    Test test = (Test) a;

    result.setEnabled(test.enabled());
    result.setGroups(join(test.groups(), findInheritedStringArray(cls, Test.class, "groups")));
    result.setParameters(test.parameters());
    result.setDependsOnGroups(join(test.dependsOnGroups(),
        findInheritedStringArray(cls, Test.class, "dependsOnGroups")));
    result.setDependsOnMethods(join(test.dependsOnMethods(),
        findInheritedStringArray(cls, Test.class, "dependsOnMethods")));
    result.setTimeOut(test.timeOut());
    result.setInvocationTimeOut(test.invocationTimeOut());
    result.setInvocationCount(test.invocationCount());
    result.setThreadPoolSize(test.threadPoolSize());
    result.setSuccessPercentage(test.successPercentage());
    result.setDataProvider(test.dataProvider());
//    result.setDataProviderClass(test.dataProviderClass() != Object.class ?
//        test.dataProviderClass() : null);
    result.setDataProviderClass(
        findInherited(test.dataProviderClass(), cls, Test.class, "dataProviderClass",
            DEFAULT_CLASS));
    result.setAlwaysRun(test.alwaysRun());
    result.setDescription(
        findInherited(test.description(), cls, Test.class, "description", DEFAULT_STRING));
    result.setExpectedExceptions(test.expectedExceptions());
    result.setExpectedExceptionsMessageRegExp(test.expectedExceptionsMessageRegExp());
    result.setSuiteName(test.suiteName());
    result.setTestName(test.testName());
    result.setSequential(test.sequential());
    result.setSingleThreaded(test.singleThreaded());
    result.setRetryAnalyzer(test.retryAnalyzer());
    result.setSkipFailedInvocations(test.skipFailedInvocations());
    result.setIgnoreMissingDependencies(test.ignoreMissingDependencies());
    result.setPriority(test.priority());

    return result;
  }

  private String[] join(String[] strings, String[] strings2) {
    List<String> result = Lists.newArrayList(strings);
    Set<String> seen = new HashSet<>(Lists.newArrayList(strings));
    for (String s : strings2) {
      if (! seen.contains(s)) {
        result.add(s);
      }
    }

    return result.toArray(new String[result.size()]);
  }

  /**
   * This interface is used to calculate the default value for various
   * annotation return types. This is used when looking for an annotation in a
   * hierarchy. We can't use null as a default since annotation don't allow
   * nulls, so each type has a different way of defining its own default.
   */
  static interface Default<T> {
    boolean isDefault(T t);
  }

  private static final Default<Class<?>> DEFAULT_CLASS = new Default<Class<?>>() {
    @Override
    public boolean isDefault(Class<?> c) {
      return c == Object.class;
    }
  };

  private static final Default<String> DEFAULT_STRING = new Default<String>() {
    @Override
    public boolean isDefault(String s) {
      return Utils.isStringEmpty(s);
    }
  };

  /**
   * Find the value of an annotation, starting with the annotation found on the
   * method, then the class and then parent classes until we either find a
   * non-default value or we reach the top of the hierarchy (Object).
   */
  private <T> T findInherited(T methodValue, Class<?> cls,
      Class<? extends Annotation> annotationClass, String methodName,
      Default<T> def) {

    // Look on the method first and return right away if the annotation is there
    if (!def.isDefault(methodValue)) {
      return methodValue;
    }

    // Not found, look on the class and then up the hierarchy
    while (cls != null && cls != Object.class) {
      Annotation annotation = cls.getAnnotation(annotationClass);
      if (annotation != null) {
        T result = (T) invokeMethod(annotation, methodName);
        if (!def.isDefault(result)) {
          return result;
        }
      }
      cls = cls.getSuperclass();
    }

    return null;
  }

  /**
   * Find the value of a String[] annotation. The difference with the
   * findInherited method above is that TestNG aggregates String[] values across
   * hierarchies. For example, of the method annotation has { "a", "b" } and the
   * class has { "c" }, the returned value will be { "a", "b", "c" }.
   */
  private String[] findInheritedStringArray(Class<?> cls,
      Class<? extends Annotation> annotationClass, String methodName)
  {
    if (null == cls) {
      return new String[0];
    }

    List<String> result = Lists.newArrayList();

    while (cls != null && cls != Object.class) {
      Annotation annotation = cls.getAnnotation(annotationClass);
      if (annotation != null) {
        String[] g = (String[]) invokeMethod(annotation, methodName);
        for (String s : g) {
          result.add(s);
        }
      }
      cls = cls.getSuperclass();
    }

    return result.toArray(new String[result.size()]);
  }

  private Object invokeMethod(Annotation test, String methodName) {
    Object result = null;
    try {
      // Note:  we should cache methods already looked up
      Method m = test.getClass().getMethod(methodName, new Class[0]);
      result = m.invoke(test, new Object[0]);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private void ppp(String string) {
    System.out.println("[JDK15TagFactory] " + string);
  }

}
