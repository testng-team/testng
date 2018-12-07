package org.testng.internal.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
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
import org.testng.internal.collections.Ints;
import org.testng.log4testng.Logger;

/**
 * This class creates implementations of IAnnotations based on the JDK5 annotation that was found on
 * the Java element.
 */
public class JDK15TagFactory {

  public <A extends IAnnotation> A createTag(
      Class<?> cls, Method method, Annotation a, Class<A> annotationClass) {
    IAnnotation result = null;

    if (a != null) {
      if (annotationClass == IDataProviderAnnotation.class) {
        result = createDataProviderTag(method, a);
      } else if (annotationClass == IFactoryAnnotation.class) {
        result = createFactoryTag(cls, a);
      } else if (annotationClass == IParametersAnnotation.class) {
        result = createParametersTag(a);
      } else if (annotationClass == IObjectFactoryAnnotation.class) {
        result = createObjectFactoryTag();
      } else if (annotationClass == ITestAnnotation.class) {
        result = createTestTag(cls, a);
      } else if (annotationClass == IListenersAnnotation.class) {
        result = createListenersTag(a);
      } else if (annotationClass == IBeforeSuite.class
          || annotationClass == IAfterSuite.class
          || annotationClass == IBeforeTest.class
          || annotationClass == IAfterTest.class
          || annotationClass == IBeforeGroups.class
          || annotationClass == IAfterGroups.class
          || annotationClass == IBeforeClass.class
          || annotationClass == IAfterClass.class
          || annotationClass == IBeforeMethod.class
          || annotationClass == IAfterMethod.class) {
        result = maybeCreateNewConfigurationTag(a, annotationClass);
      } else {
        throw new TestNGException("Unknown annotation requested:" + annotationClass);
      }
    }

    //noinspection unchecked
    return (A) result;
  }

  private IAnnotation maybeCreateNewConfigurationTag(Annotation a, Class<?> annotationClass) {
    IAnnotation result = null;

    if (annotationClass == IBeforeSuite.class) {
      BeforeSuite bs = (BeforeSuite) a;
      result =
          createConfigurationTag(
              true,
              false,
              false,
              false,
              new String[0],
              new String[0],
              false,
              false,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IAfterSuite.class) {
      AfterSuite bs = (AfterSuite) a;
      result =
          createConfigurationTag(
              false,
              true,
              false,
              false,
              new String[0],
              new String[0],
              false,
              false,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IBeforeTest.class) {
      BeforeTest bs = (BeforeTest) a;
      result =
          createConfigurationTag(
              false,
              false,
              true,
              false,
              new String[0],
              new String[0],
              false,
              false,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IAfterTest.class) {
      AfterTest bs = (AfterTest) a;
      result =
          createConfigurationTag(
              false,
              false,
              false,
              true,
              new String[0],
              new String[0],
              false,
              false,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IBeforeGroups.class) {
      BeforeGroups bs = (BeforeGroups) a;
      final String[] groups = bs.value().length > 0 ? bs.value() : bs.groups();
      result =
          createConfigurationTag(
              false,
              false,
              false,
              false,
              groups,
              new String[0],
              false,
              false,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IAfterGroups.class) {
      AfterGroups bs = (AfterGroups) a;
      final String[] groups = bs.value().length > 0 ? bs.value() : bs.groups();
      result =
          createConfigurationTag(
              false,
              false,
              false,
              false,
              new String[0],
              groups,
              false,
              false,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IBeforeClass.class) {
      BeforeClass bs = (BeforeClass) a;
      result =
          createConfigurationTag(
              false,
              false,
              false,
              false,
              new String[0],
              new String[0],
              true,
              false,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IAfterClass.class) {
      AfterClass bs = (AfterClass) a;
      result =
          createConfigurationTag(
              false,
              false,
              false,
              false,
              new String[0],
              new String[0],
              false,
              true,
              false,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              false,
              bs.timeOut(),
              new String[0]);
    } else if (annotationClass == IBeforeMethod.class) {
      BeforeMethod bs = (BeforeMethod) a;
      result =
          createConfigurationTag(
              false,
              false,
              false,
              false,
              new String[0],
              new String[0],
              false,
              false,
              true,
              false,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              bs.firstTimeOnly(),
              false,
              bs.timeOut(),
              bs.onlyForGroups());
    } else if (annotationClass == IAfterMethod.class) {
      AfterMethod bs = (AfterMethod) a;
      result =
          createConfigurationTag(
              false,
              false,
              false,
              false,
              new String[0],
              new String[0],
              false,
              false,
              false,
              true,
              bs.alwaysRun(),
              bs.dependsOnGroups(),
              bs.dependsOnMethods(),
              bs.description(),
              bs.enabled(),
              bs.groups(),
              bs.inheritGroups(),
              false,
              bs.lastTimeOnly(),
              bs.timeOut(),
              bs.onlyForGroups());
    }

    return result;
  }

  private IAnnotation createConfigurationTag(
      boolean beforeSuite,
      boolean afterSuite,
      boolean beforeTest,
      boolean afterTest,
      String[] beforeGroups,
      String[] afterGroups,
      boolean beforeClass,
      boolean afterClass,
      boolean beforeMethod,
      boolean afterMethod,
      boolean alwaysRun,
      String[] dependsOnGroups,
      String[] dependsOnMethods,
      String description,
      boolean enabled,
      String[] groups,
      boolean inheritGroups,
      boolean firstTimeOnly,
      boolean lastTimeOnly,
      long timeOut,
      String[] groupFilters) {
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
    result.setGroupFilters(groupFilters);
    result.setFirstTimeOnly(firstTimeOnly);
    result.setLastTimeOnly(lastTimeOnly);
    result.setTimeOut(timeOut);

    return result;
  }

  private IAnnotation createDataProviderTag(Method method, Annotation a) {
    DataProviderAnnotation result = new DataProviderAnnotation();
    DataProvider c = (DataProvider) a;
    if (c.name().isEmpty()) {
      result.setName(method.getName());
    } else {
      result.setName(c.name());
    }
    result.setParallel(c.parallel());
    result.setIndices(Ints.asList(c.indices()));

    return result;
  }

  private IAnnotation createFactoryTag(Class<?> cls, Annotation a) {
    FactoryAnnotation result = new FactoryAnnotation();
    Factory c = (Factory) a;
    Class<?> dpc =
        findInherited(
            c.dataProviderClass(), cls, Factory.class, "dataProviderClass", DEFAULT_CLASS);
    result.setDataProvider(c.dataProvider());
    result.setDataProviderClass(dpc == null || dpc == Object.class ? cls : dpc);
    result.setEnabled(c.enabled());
    result.setIndices(Ints.asList(c.indices()));

    return result;
  }

  private IAnnotation createObjectFactoryTag() {
    return new ObjectFactoryAnnotation();
  }

  private IAnnotation createParametersTag(Annotation a) {
    ParametersAnnotation result = new ParametersAnnotation();
    Parameters c = (Parameters) a;
    result.setValue(c.value());

    return result;
  }

  private IAnnotation createListenersTag(Annotation a) {
    ListenersAnnotation result = new ListenersAnnotation();
    Listeners l = (Listeners) a;
    result.setValue(l.value());

    return result;
  }

  private IAnnotation createTestTag(Class<?> cls, Annotation a) {
    TestAnnotation result = new TestAnnotation();
    Test test = (Test) a;

    result.setEnabled(test.enabled());
    result.setGroups(join(test.groups(), findInheritedStringArray(cls, "groups")));
    result.setDependsOnGroups(
        join(test.dependsOnGroups(), findInheritedStringArray(cls, "dependsOnGroups")));
    result.setDependsOnMethods(
        join(
            test.dependsOnMethods(),
            findInheritedStringArray(cls, "dependsOnMethods")));
    result.setTimeOut(test.timeOut());
    result.setInvocationTimeOut(test.invocationTimeOut());
    result.setInvocationCount(test.invocationCount());
    result.setThreadPoolSize(test.threadPoolSize());
    result.setSuccessPercentage(test.successPercentage());
    result.setDataProvider(test.dataProvider());
    result.setDataProviderClass(
        findInherited(
            test.dataProviderClass(), cls, Test.class, "dataProviderClass", DEFAULT_CLASS));
    result.setAlwaysRun(test.alwaysRun());
    result.setDescription(
        findInherited(test.description(), cls, Test.class, "description", DEFAULT_STRING));
    result.setExpectedExceptions(test.expectedExceptions());
    result.setExpectedExceptionsMessageRegExp(test.expectedExceptionsMessageRegExp());
    result.setSuiteName(test.suiteName());
    result.setTestName(test.testName());
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
      if (!seen.contains(s)) {
        result.add(s);
      }
    }

    return result.toArray(new String[0]);
  }

  /**
   * This interface is used to calculate the default value for various annotation return types. This
   * is used when looking for an annotation in a hierarchy. We can't use null as a default since
   * annotation don't allow nulls, so each type has a different way of defining its own default.
   */
  interface Default<T> {
    boolean isDefault(T t);
  }

  private static final Default<Class<?>> DEFAULT_CLASS = c -> c == Object.class;

  private static final Default<String> DEFAULT_STRING = Utils::isStringEmpty;

  /**
   * Find the value of an annotation, starting with the annotation found on the method, then the
   * class and then parent classes until we either find a non-default value or we reach the top of
   * the hierarchy (Object).
   */
  @SuppressWarnings("unchecked")
  private <T> T findInherited(
      T methodValue,
      Class<?> cls,
      Class<? extends Annotation> annotationClass,
      String methodName,
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
   * Find the value of a String[] annotation. The difference with the findInherited method above is
   * that TestNG aggregates String[] values across hierarchies. For example, of the method
   * annotation has { "a", "b" } and the class has { "c" }, the returned value will be { "a", "b",
   * "c" }.
   */
  private String[] findInheritedStringArray(Class<?> cls, String methodName) {
    if (null == cls) {
      return new String[0];
    }

    List<String> result = Lists.newArrayList();

    while (cls != null && cls != Object.class) {
      Annotation annotation = cls.getAnnotation(Test.class);
      if (annotation != null) {
        String[] g = (String[]) invokeMethod(annotation, methodName);
        result.addAll(Arrays.asList(g));
      }
      cls = cls.getSuperclass();
    }

    return result.toArray(new String[0]);
  }

  private Object invokeMethod(Annotation test, String methodName) {
    Object result = null;
    try {
      // Note:  we should cache methods already looked up
      Method m = test.getClass().getMethod(methodName);
      result = m.invoke(test);
    } catch (Exception e) {
      Logger.getLogger(JDK15TagFactory.class).error(e.getMessage(), e);
    }
    return result;
  }
}
