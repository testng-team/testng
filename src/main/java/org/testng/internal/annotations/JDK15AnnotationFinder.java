package org.testng.internal.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

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
import org.testng.annotations.Listeners;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.TestInstance;
import org.testng.collections.Maps;
import org.testng.internal.collections.Pair;

/**
 * This class implements IAnnotationFinder with JDK5 annotations
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JDK15AnnotationFinder implements IAnnotationFinder {
  private JDK15TagFactory m_tagFactory = new JDK15TagFactory();
  private Map<Class<? extends IAnnotation>, Class<? extends Annotation>> m_annotationMap = Maps.newHashMap();
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
    m_annotationMap.put(IListeners.class, Listeners.class);
  }

  private <A extends Annotation> A findAnnotationInSuperClasses(Class cls, Class<A> a) {
    // Hack for @Listeners: we don't look in superclasses for this annotation
    // because inheritance of this annotation causes aggregation instead of
    // overriding
    if (a.equals(org.testng.annotations.Listeners.class)) {
      return (A) cls.getAnnotation(a);
    }
    else {
      while (cls != null) {
        A result = (A) cls.getAnnotation(a);
        if (result != null) {
          return result;
        } else {
          cls = cls.getSuperclass();
        }
      }
    }

    return null;
  }

  @Override
  public <A extends IAnnotation> A findAnnotation(Method m, Class<A> annotationClass) {
    final Class<? extends Annotation> a = m_annotationMap.get(annotationClass);
    if (a == null) {
      throw new IllegalArgumentException("Java @Annotation class for '" + annotationClass + "' not found.");
    }
    return findAnnotation(m.getDeclaringClass(), m.getAnnotation(a), annotationClass, null, null, m);
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

  @Override
  public <A extends IAnnotation> A findAnnotation(Class cls, Class<A> annotationClass) {
    final Class<? extends Annotation> a = m_annotationMap.get(annotationClass);
    if (a == null) {
      throw new IllegalArgumentException("Java @Annotation class for '" + annotationClass + "' not found.");
    }
    return findAnnotation(cls, findAnnotationInSuperClasses(cls, a), annotationClass, cls, null, null);
  }

  @Override
  public <A extends IAnnotation> A findAnnotation(Constructor cons, Class<A> annotationClass) {
    final Class<? extends Annotation> a = m_annotationMap.get(annotationClass);
    if (a == null) {
      throw new IllegalArgumentException("Java @Annotation class for '" + annotationClass + "' not found.");
    }
    return findAnnotation(cons.getDeclaringClass(), cons.getAnnotation(a), annotationClass, null, cons, null);
  }

  private Map<Pair<Annotation, ?>, IAnnotation> m_annotations = Maps.newHashMap();

  private <A extends IAnnotation> A findAnnotation(Class cls, Annotation a,
                                                   Class<A> annotationClass,
                                                   Class testClass, Constructor testConstructor, Method testMethod) {
    final Pair<Annotation, ?> p;
    if (testClass != null) {
      p = new Pair<Annotation, Class>(a, testClass);
    } else if (testConstructor != null) {
      p = new Pair<Annotation, Constructor>(a, testConstructor);
    } else {
      p = new Pair<Annotation, Method>(a, testMethod);
    }
    //noinspection unchecked
    A result = (A) m_annotations.get(p);
    if (result == null) {
      result = m_tagFactory.createTag(cls, a, annotationClass, m_transformer);
      m_annotations.put(p, result);
      transform(result, testClass, testConstructor, testMethod);
    }
    //noinspection unchecked
    return result;
  }

  @Override
  public boolean hasTestInstance(Method method, int i) {
    final Annotation[][] annotations = method.getParameterAnnotations();
    if (annotations.length > 0 && annotations[i].length > 0) {
      final Annotation[] pa = annotations[i];
      for (Annotation a : pa) {
        if (a instanceof TestInstance) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String[] findOptionalValues(Method method) {
    return optionalValues(method.getParameterAnnotations());
  }

  @Override
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
