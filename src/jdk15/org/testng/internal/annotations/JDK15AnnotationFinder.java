package org.testng.internal.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class implements IAnnotationFinder with JDK5 annotations
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JDK15AnnotationFinder implements IAnnotationFinder {
  private JDK15TagFactory m_tagFactory = new JDK15TagFactory();
  private Map<Class<?>, Class<?>> m_annotationMap = 
    new HashMap<Class<?>, Class<?>>();
  private IAnnotationTransformer m_transformer = null;
  
  public JDK15AnnotationFinder(IAnnotationTransformer transformer) {
    m_transformer = transformer;
    
    m_annotationMap.put(IConfiguration.class, Configuration.class);
    m_annotationMap.put(IDataProvider.class, DataProvider.class);
    m_annotationMap.put(IExpectedExceptions.class, ExpectedExceptions.class);
    m_annotationMap.put(IFactory.class, Factory.class);
    m_annotationMap.put(IParameters.class, Parameters.class);
    m_annotationMap.put(ITest.class, Test.class);
    m_annotationMap.put(IBeforeSuite.class, BeforeSuite.class);
    m_annotationMap.put(IAfterSuite.class, AfterSuite.class);
    m_annotationMap.put(IBeforeTest.class, BeforeTest.class);
    m_annotationMap.put(IAfterTest.class, AfterTest.class);
    m_annotationMap.put(IBeforeClass.class, BeforeClass.class);
    m_annotationMap.put(IAfterClass.class, AfterClass.class);
    m_annotationMap.put(IBeforeGroups.class, BeforeGroups.class);
    m_annotationMap.put(IAfterGroups.class, AfterGroups.class);
    m_annotationMap.put(IBeforeMethod.class, BeforeMethod.class);
    m_annotationMap.put(IAfterMethod.class, AfterMethod.class);
  }

  public IAnnotation findAnnotation(Class cls, Class annotationClass) {
    Class a = m_annotationMap.get(annotationClass);
    return findAnnotation(cls, findAnnotationInSuperClasses(cls, a), annotationClass);
  }

  private Annotation findAnnotationInSuperClasses(Class cls, Class a) {
    while (cls != null) {
      Annotation result = cls.getAnnotation(a);
      if (result != null) return result;
      else cls = cls.getSuperclass();
    }
    
    return null;
  }

  public IAnnotation findAnnotation(Method m, Class annotationClass) {
    Class a = m_annotationMap.get(annotationClass);
    assert a != null : "Annotation class not found:" + annotationClass;
    return findAnnotation(m.getDeclaringClass(), m.getAnnotation(a), annotationClass);
  }
  
  private void ppp(String string) {
    System.out.println("[JDK15AnnotationFinder] " + string);
  }

  public IAnnotation findAnnotation(Constructor m, Class annotationClass) {
    Class a = m_annotationMap.get(annotationClass);
    return findAnnotation(m.getDeclaringClass(), m.getAnnotation(a), annotationClass);
  }

  public IAnnotation findAnnotation(Class cls, Annotation a, Class annotationClass) {
    return m_tagFactory.createTag(cls, a, annotationClass);
  }

  public void addSourceDirs(String[] dirs) {
    // no-op for JDK 15
  }
}
