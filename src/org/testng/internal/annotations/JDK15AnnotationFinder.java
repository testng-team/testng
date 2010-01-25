package org.testng.internal.annotations;

import org.testng.IAnnotationTransformer;
import org.testng.IAnnotationTransformer2;
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
import org.testng.annotations.IObjectFactoryAnnotation;
import org.testng.annotations.IParametersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.TestInstance;
import org.testng.collections.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This class implements IAnnotationFinder with JDK5 annotations
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JDK15AnnotationFinder implements IAnnotationFinder {
  private JDK15TagFactory m_tagFactory = new JDK15TagFactory();
  private Map<Class<?>, Class<?>> m_annotationMap = Maps.newHashMap();
  private IAnnotationTransformer m_transformer = null;
  
  @SuppressWarnings({"deprecation"})
  public JDK15AnnotationFinder(IAnnotationTransformer transformer) {
    m_transformer = transformer;
    
    m_annotationMap.put(IConfigurationAnnotation.class, Configuration.class);
    m_annotationMap.put(IDataProviderAnnotation.class, DataProvider.class);
    m_annotationMap.put(IExpectedExceptionsAnnotation.class, ExpectedExceptions.class);
    m_annotationMap.put(IFactoryAnnotation.class, Factory.class);
    m_annotationMap.put(IObjectFactoryAnnotation.class, ObjectFactory.class);
    m_annotationMap.put(IParametersAnnotation.class, Parameters.class);
    m_annotationMap.put(ITestAnnotation.class, Test.class);
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
    IAnnotation result =
      findAnnotation(m.getDeclaringClass(), m.getAnnotation(a), annotationClass,
          null, null, m);
    
    return result;
  }
  
  private void transform(IAnnotation a, Class testClass,
      Constructor testConstructor, Method testMethod)
  {
    //
    // Transform @Test
    //
    if (a instanceof ITestAnnotation) {
      m_transformer.transform((ITestAnnotation) a, testClass, testConstructor, testMethod);
    }
    
    else if (m_transformer instanceof IAnnotationTransformer2) {
      IAnnotationTransformer2 transformer2 = (IAnnotationTransformer2) m_transformer;

      //
      // Transform a configuration annotation
      //
      if (a instanceof IConfigurationAnnotation) {
        IConfigurationAnnotation configuration = (IConfigurationAnnotation) a;
        transformer2.transform(configuration,testClass, testConstructor, testMethod);
      }
      
      //
      // Transform @DataProvider
      //
      else if (a instanceof IDataProviderAnnotation) {
        transformer2.transform((IDataProviderAnnotation) a, testMethod);
      }

      //
      // Transform @Factory
      //
      else if (a instanceof IFactoryAnnotation) {
        transformer2.transform((IFactoryAnnotation) a, testMethod);
      }
    }
  }
  
  public IAnnotation findAnnotation(Class cls, Class annotationClass) {
    Class a = m_annotationMap.get(annotationClass);
    IAnnotation result =
      findAnnotation(cls, findAnnotationInSuperClasses(cls, a), annotationClass,
          cls, null, null);

    return result;
  }
  
  public IAnnotation findAnnotation(Constructor m, Class annotationClass) {
    Class a = m_annotationMap.get(annotationClass);
    IAnnotation result =
      findAnnotation(m.getDeclaringClass(), m.getAnnotation(a), annotationClass,
          null, m, null);
    
    return result;
  }

  private Map<Pair, IAnnotation> m_annotations = Maps.newHashMap();

  private IAnnotation findAnnotation(Class cls, Annotation a, 
      Class annotationClass,
      Class testClass, Constructor testConstructor, Method testMethod) 
  {

    IAnnotation result = null;
    Pair<Annotation, Class> p1;
    Pair<Annotation, Constructor> p2;
    Pair<Annotation, Class> p3;
    
    Pair p;
    if (testClass != null) {
      p = new Pair(a, testClass);
    }
    else if (testConstructor != null) {
      p = new Pair(a, testConstructor);
    }
    else {
      p = new Pair(a, testMethod);
    }
    result = m_annotations.get(p);
    if (result == null) {
      result = m_tagFactory.createTag(cls, a, annotationClass, m_transformer);
      m_annotations.put(p, result);
      transform(result, testClass, testConstructor, testMethod);
    }

    return result;
  }
  
  class Pair<A, B> {
    public A a;
    public B b;

    public Pair(A a, B b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((a == null) ? 0 : a.hashCode());
      result = prime * result + ((b == null) ? 0 : b.hashCode());
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final Pair other = (Pair) obj;
      if (a == null) {
        if (other.a != null)
          return false;
      }
      else if (!a.equals(other.a))
        return false;
      if (b == null) {
        if (other.b != null)
          return false;
      }
      else if (!b.equals(other.b))
        return false;
      return true;
    }
    
    
  }

  private void ppp(String string) {
    System.out.println("[JDK15AnnotationFinder] " + string);
  }
  
  public void addSourceDirs(String[] dirs) {
    // no-op for JDK 15
  }

  public boolean hasTestInstance(Method method, int i) {
    boolean result = false;
    Annotation[][] annotations = method.getParameterAnnotations();
    if (annotations.length > 0 && annotations[i].length > 0) {
      Annotation a = annotations[i][0];
      result = a instanceof TestInstance;
    }
    
    return result;
  }
  
  public String[] findOptionalValues(Method method) {
    return optionalValues(method.getParameterAnnotations());
  }
  
  public String[] findOptionalValues(Constructor method) {
    return optionalValues(method.getParameterAnnotations());
  }

  private String[] optionalValues(Annotation[][] annotations) {
    String[] result = new String[annotations.length];
    for (int i = 0; i < annotations.length; i++) {
      for (Annotation a : annotations[i]) {
        if (a instanceof Optional) {
          result[i] = ((Optional)a).value();
          break;
        }
      }
    }
    return result;
  }
}
