package org.testng.internal;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.IDataProviderListener;
import org.testng.IInstanceInfo;
import org.testng.IObjectFactory;
import org.testng.IObjectFactory2;
import org.testng.ITestContext;
import org.testng.ITestMethodFinder;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/** This class represents a method annotated with @Factory */
public class FactoryMethod extends BaseTestMethod {

  private final IFactoryAnnotation factoryAnnotation;
  private final Object m_instance;
  private final ITestContext m_testContext;
  private final ITestObjectFactory objectFactory;
  private final Map<Class<? extends IDataProviderListener>, IDataProviderListener>
      m_dataProviderListeners;

  @SuppressWarnings("unchecked")
  private void init(Object instance, IAnnotationFinder annotationFinder, ConstructorOrMethod com) {
    IListenersAnnotation annotation =
        annotationFinder.findAnnotation(com.getDeclaringClass(), IListenersAnnotation.class);
    if (annotation == null) {
      return;
    }
    Class<? extends ITestNGListener>[] listeners = annotation.getValue();
    for (Class<? extends ITestNGListener> listener : listeners) {
      if (!IDataProviderListener.class.isAssignableFrom(listener)) {
        continue;
      }

      Class<? extends IDataProviderListener> key =
          (Class<? extends IDataProviderListener>) listener;
      if (m_dataProviderListeners.containsKey(key)) {
        continue;
      }

      if (instance != null && IDataProviderListener.class.isAssignableFrom(instance.getClass())) {
        m_dataProviderListeners.put(key, (IDataProviderListener) instance);
        continue;
      }

      Object object = ClassHelper.newInstanceOrNull(listener);
      if (object != null) {
        m_dataProviderListeners.put(key, (IDataProviderListener) object);
      }
    }
  }

  // This constructor is intentionally created with package visibility because we dont have any
  // callers of this
  // constructor outside of this package.
  FactoryMethod(
      ConstructorOrMethod com,
      Object instance,
      IAnnotationFinder annotationFinder,
      ITestContext testContext,
      ITestObjectFactory objectFactory,
      Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
    super(com.getName(), com, annotationFinder, instance);
    m_dataProviderListeners = dataProviderListeners;
    init(instance, annotationFinder, com);
    Utils.checkInstanceOrStatic(instance, com.getMethod());
    Utils.checkReturnType(com.getMethod(), Object[].class, IInstanceInfo[].class);
    Class<?> declaringClass = com.getDeclaringClass();
    if (instance != null && !declaringClass.isAssignableFrom(instance.getClass())) {
      throw new TestNGException(
          "Mismatch between instance/method classes:" + instance.getClass() + " " + declaringClass);
    }
    if (instance == null
        && com.getMethod() != null
        && !Modifier.isStatic(com.getMethod().getModifiers())) {
      throw new TestNGException(
          "An inner factory method MUST be static. But '"
              + com.getMethod().getName()
              + "' from '"
              + declaringClass.getName()
              + "' is not.");
    }
    if (com.getMethod() != null && !Modifier.isPublic(com.getMethod().getModifiers())) {
      try {
        com.getMethod().setAccessible(true);
      } catch (SecurityException e) {
        throw new TestNGException(com.getMethod().getName() + " must be public", e);
      }
    }

    factoryAnnotation = annotationFinder.findAnnotation(com, IFactoryAnnotation.class);

    m_instance = instance;
    m_testContext = testContext;
    NoOpTestClass tc = new NoOpTestClass();
    tc.setTestClass(declaringClass);
    m_testClass = tc;
    this.objectFactory = objectFactory;
    m_groups = getAllGroups(declaringClass, testContext.getCurrentXmlTest(), annotationFinder);
  }

  private static String[] getAllGroups(
      Class<?> declaringClass, XmlTest xmlTest, IAnnotationFinder annotationFinder) {
    // Find the groups of the factory => all groups of all test methods
    ITestMethodFinder testMethodFinder = new TestNGMethodFinder(new RunInfo(), annotationFinder);
    ITestNGMethod[] testMethods = testMethodFinder.getTestMethods(declaringClass, xmlTest);
    Set<String> groups = new HashSet<>();
    for (ITestNGMethod method : testMethods) {
      groups.addAll(Arrays.asList(method.getGroups()));
    }
    return groups.toArray(new String[0]);
  }

  public Object[] invoke() {
    List<Object> result = Lists.newArrayList();

    Map<String, String> allParameterNames = Maps.newHashMap();
    Parameters.MethodParameters methodParameters =
        new Parameters.MethodParameters(
            m_testContext.getCurrentXmlTest().getAllParameters(),
            findMethodParameters(m_testContext.getCurrentXmlTest()),
            null,
            null,
            m_testContext,
            null /* testResult */);

    Iterator<Object[]> parameterIterator =
        Parameters.handleParameters(
                this,
                allParameterNames,
                m_instance,
                methodParameters,
                m_testContext.getCurrentXmlTest().getSuite(),
                m_annotationFinder,
                null /* fedInstance */,
                m_dataProviderListeners.values(),
                "@Factory")
            .parameters;

    try {
      List<Integer> indices = factoryAnnotation.getIndices();
      int position = 0;
      while (parameterIterator.hasNext()) {
        Object[] parameters = parameterIterator.next();
        if (parameters == null) {
          // skipped value
          continue;
        }
        ConstructorOrMethod com = getConstructorOrMethod();
        if (com.getMethod() != null) {
          Object[] testInstances = (Object[]) com.getMethod().invoke(m_instance, parameters);
          if (indices == null || indices.isEmpty()) {
            result.addAll(Arrays.asList(testInstances));
          } else {
            for (Integer index : indices) {
              int i = index - position;
              if (i >= 0 && i < testInstances.length) {
                result.add(testInstances[i]);
              }
            }
          }
          position += testInstances.length;
        } else {
          if (indices == null || indices.isEmpty() || indices.contains(position)) {
            Object instance;
            if (objectFactory instanceof IObjectFactory) {
              instance =
                  ((IObjectFactory) objectFactory).newInstance(com.getConstructor(), parameters);
            } else if (objectFactory instanceof IObjectFactory2) {
              instance = ((IObjectFactory2) objectFactory).newInstance(com.getDeclaringClass());
            } else {
              throw new IllegalStateException(
                  "Unsupported ITestObjectFactory " + objectFactory.getClass());
            }
            result.add(instance);
          }
          position++;
        }
      }
    } catch (Throwable t) {
      ConstructorOrMethod com = getConstructorOrMethod();
      throw new TestNGException(
          "The factory method "
              + com.getDeclaringClass()
              + "."
              + com.getName()
              + "() threw an exception",
          t);
    }

    return result.toArray(new Object[0]);
  }

  @Override
  public ITestNGMethod clone() {
    throw new IllegalStateException("clone is not supported for FactoryMethod");
  }
}
