package org.testng.internal;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.IInstanceInfo;
import org.testng.IObjectFactory;
import org.testng.IObjectFactory2;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/**
 * This class represents a method annotated with @Factory
 */
public class FactoryMethod extends BaseTestMethod {

  private static final long serialVersionUID = -7329918821346197099L;

  private final Object m_instance;
  private final XmlTest m_xmlTest;
  private final ITestContext m_testContext;
  private final ITestObjectFactory objectFactory;

  public FactoryMethod(ConstructorOrMethod com, Object instance, XmlTest xmlTest, IAnnotationFinder annotationFinder,
                       ITestContext testContext, ITestObjectFactory objectFactory) {
    super(com.getName(), com, annotationFinder, instance);
    Utils.checkInstanceOrStatic(instance, com.getMethod());
    Utils.checkReturnType(com.getMethod(), Object[].class, IInstanceInfo[].class);
    Class<?> declaringClass = com.getDeclaringClass();
    if (instance != null && ! declaringClass.isAssignableFrom(instance.getClass())) {
      throw new TestNGException("Mismatch between instance/method classes:"
          + instance.getClass() + " " + declaringClass);
    }
    if (instance == null && com.getMethod() != null && !Modifier.isStatic(com.getMethod().getModifiers())) {
      throw new TestNGException("An inner factory method MUST be static. But '" + com.getMethod().getName() + "' from '" + declaringClass.getName() + "' is not.");
    }
    if (com.getMethod() != null && !Modifier.isPublic(com.getMethod().getModifiers())) {
      try {
        com.getMethod().setAccessible(true);
      } catch (SecurityException e) {
        throw new TestNGException(e);
      }
    }

    m_instance = instance;
    m_xmlTest = xmlTest;
    m_testContext = testContext;
    NoOpTestClass tc = new NoOpTestClass();
    tc.setTestClass(declaringClass);
    m_testClass = tc;
    this.objectFactory = objectFactory;
  }

  public Object[] invoke() {
    List<Object> result = Lists.newArrayList();

    Map<String, String> allParameterNames = Maps.newHashMap();
    Iterator<Object[]> parameterIterator =
      Parameters.handleParameters(this,
          allParameterNames,
          m_instance,
          new Parameters.MethodParameters(m_xmlTest.getAllParameters(),
              findMethodParameters(m_xmlTest),
              null, null, m_testContext,
              null /* testResult */),
          m_xmlTest.getSuite(),
          m_annotationFinder,
          null /* fedInstance */).parameters;

    try {
      while (parameterIterator.hasNext()) {
        Object[] parameters = parameterIterator.next();
        Object[] testInstances;
        ConstructorOrMethod com = getConstructorOrMethod();
        if (com.getMethod() != null) {
          testInstances = (Object[]) getMethod().invoke(m_instance, parameters);
          for (Object testInstance : testInstances) {
            result.add(testInstance);
          }
        } else {
          Object instance;
          if (objectFactory instanceof IObjectFactory) {
            instance = ((IObjectFactory) objectFactory).newInstance(com.getConstructor(), parameters);
          } else if (objectFactory instanceof IObjectFactory2) {
            instance = ((IObjectFactory2) objectFactory).newInstance(com.getDeclaringClass());
          } else {
            throw new IllegalStateException("Unsupported ITestObjectFactory " + objectFactory.getClass());
          }
          result.add(instance);
        }
      }
    } catch (Throwable t) {
      ConstructorOrMethod com = getConstructorOrMethod();
      throw new TestNGException("The factory method "
          + com.getDeclaringClass() + "." + com.getName()
          + "() threw an exception", t);
    }

    return result.toArray(new Object[result.size()]);
  }

  @Override
  public ITestNGMethod clone() {
    throw new IllegalStateException("clone is not supported for FactoryMethod");
  }
}
