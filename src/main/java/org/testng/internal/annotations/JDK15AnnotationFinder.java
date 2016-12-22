package org.testng.internal.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.testng.IAnnotationTransformer;
import org.testng.IAnnotationTransformer2;
import org.testng.IAnnotationTransformer3;
import org.testng.ITestNGMethod;
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
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.TestInstance;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.collections.Pair;

/**
 * This class implements IAnnotationFinder with JDK5 annotations
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class JDK15AnnotationFinder implements IAnnotationFinder {
  private JDK15TagFactory tagFactory = new JDK15TagFactory();
  private Map<Class<? extends IAnnotation>, Class<? extends Annotation>> annotationMap = new ConcurrentHashMap<>();
  private Map<Pair<Annotation, ?>, IAnnotation> annotations = new ConcurrentHashMap<>();
  private static final String ERR_FMT = "Java @Annotation class for '%s' not found.";

  private IAnnotationTransformer transformer = null;

  @SuppressWarnings({"deprecation"})
  public JDK15AnnotationFinder(IAnnotationTransformer transformer) {
    this.transformer = transformer;
    annotationMap.put(IListenersAnnotation.class, Listeners.class);
    annotationMap.put(IConfigurationAnnotation.class, Configuration.class);
    annotationMap.put(IDataProviderAnnotation.class, DataProvider.class);
    annotationMap.put(IExpectedExceptionsAnnotation.class, ExpectedExceptions.class);
    annotationMap.put(IFactoryAnnotation.class, Factory.class);
    annotationMap.put(IObjectFactoryAnnotation.class, ObjectFactory.class);
    annotationMap.put(IParametersAnnotation.class, Parameters.class);
    annotationMap.put(ITestAnnotation.class, Test.class);
    // internal
    annotationMap.put(IBeforeSuite.class, BeforeSuite.class);
    annotationMap.put(IAfterSuite.class, AfterSuite.class);
    annotationMap.put(IBeforeTest.class, BeforeTest.class);
    annotationMap.put(IAfterTest.class, AfterTest.class);
    annotationMap.put(IBeforeClass.class, BeforeClass.class);
    annotationMap.put(IAfterClass.class, AfterClass.class);
    annotationMap.put(IBeforeGroups.class, BeforeGroups.class);
    annotationMap.put(IAfterGroups.class, AfterGroups.class);
    annotationMap.put(IBeforeMethod.class, BeforeMethod.class);
    annotationMap.put(IAfterMethod.class, AfterMethod.class);
  }

  private <A extends Annotation> A findAnnotationInSuperClasses(Class<?> clazz, Class<A> a) {
    Class<?> cls = clazz;
    // Hack for @Listeners: we don't look in superclasses for this annotation
    // because inheritance of this annotation causes aggregation instead of
    // overriding
    if (a.equals(org.testng.annotations.Listeners.class)) {
      return cls.getAnnotation(a);
    }
    else {
      while (cls != null) {
        A result = cls.getAnnotation(a);
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
    final Class<? extends Annotation> a = annotationMap.get(annotationClass);
    if (a == null) {
      throw new IllegalArgumentException(String.format(ERR_FMT,annotationClass));
    }
    Annotation annotation = m.getAnnotation(a);
    return findAnnotation(m.getDeclaringClass(), annotation, annotationClass, null, null, m,
        new Pair<>(annotation, m));
  }

  @Override
  public <A extends IAnnotation> A findAnnotation(ITestNGMethod tm, Class<A> annotationClass) {
    final Class<? extends Annotation> a = annotationMap.get(annotationClass);
    if (a == null) {
      throw new IllegalArgumentException(String.format(ERR_FMT,annotationClass));
    }
    Method m = tm.getConstructorOrMethod().getMethod();
    Class<?> testClass;
    if (tm.getInstance() == null) {
      testClass = m.getDeclaringClass();
    } else {
      testClass = tm.getInstance().getClass();
    }
    Annotation annotation = m.getAnnotation(a);
    if (annotation == null) {
      annotation = testClass.getAnnotation(a);
    }
    return findAnnotation(testClass, annotation, annotationClass, null, null, m,
        new Pair<>(annotation, m));
  }

  @Override
  public <A extends IAnnotation> A findAnnotation(ConstructorOrMethod com, Class<A> annotationClass) {
    if (com.getConstructor() != null) {
      return findAnnotation(com.getConstructor(), annotationClass);
    }
    if (com.getMethod() != null) {
      return findAnnotation(com.getMethod(), annotationClass);
    }
    return null;
  }

  private void transform(IAnnotation a, Class<?> testClass,
                         Constructor<?> testConstructor, Method testMethod)  {
    //
    // Transform @Test
    //
    if (a instanceof ITestAnnotation) {
      transformer.transform((ITestAnnotation) a, testClass, testConstructor, testMethod);
    }

    else if (transformer instanceof IAnnotationTransformer2) {
      IAnnotationTransformer2 transformer2 = (IAnnotationTransformer2) transformer;

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

      else if (transformer instanceof IAnnotationTransformer3) {
        IAnnotationTransformer3 transformerThree = (IAnnotationTransformer3) this.transformer;

        //
        // Transform @Listeners
        //
        if (a instanceof IListenersAnnotation) {
          transformerThree.transform((IListenersAnnotation)a, testClass);
        }
      } // End IAnnotationTransformer3
    } // End IAnnotationTransformer2
  }

  @Override
  public <A extends IAnnotation> A findAnnotation(Class<?> cls, Class<A> annotationClass) {
    final Class<? extends Annotation> a = annotationMap.get(annotationClass);
    if (a == null) {
      throw new IllegalArgumentException("Java @Annotation class for '"
          + annotationClass + "' not found.");
    }
    Annotation annotation = findAnnotationInSuperClasses(cls, a);
    return findAnnotation(cls, annotation, annotationClass, cls, null, null,
        new Pair<>(annotation, annotationClass));
  }

  @Override
  public <A extends IAnnotation> A findAnnotation(Constructor<?> cons, Class<A> annotationClass) {
    final Class<? extends Annotation> a = annotationMap.get(annotationClass);
    if (a == null) {
      throw new IllegalArgumentException(String.format(ERR_FMT,annotationClass));
    }
    Annotation annotation = cons.getAnnotation(a);
    return findAnnotation(cons.getDeclaringClass(), annotation, annotationClass, null, cons, null,
        new Pair<>(annotation, cons));
  }

  private <A extends IAnnotation> A findAnnotation(Class cls, Annotation a,
      Class<A> annotationClass, Class<?> testClass,
      Constructor<?> testConstructor, Method testMethod, Pair<Annotation, ?> p) {
    if (a == null) {
      return null;
    }

    IAnnotation result = annotations.get(p);
    if (result == null) {
      result = tagFactory.createTag(cls, testMethod, a, annotationClass, transformer);
      annotations.put(p, result);
      transform(result, testClass, testConstructor, testMethod);
    }
    //noinspection unchecked
    return (A) result;
  }

  @Override
  public boolean hasTestInstance(Method method, int i) {
    final Annotation[][] methodParameterAnnotations = method.getParameterAnnotations();
    if (methodParameterAnnotations.length > 0 && methodParameterAnnotations[i].length > 0) {
      final Annotation[] pa = methodParameterAnnotations[i];
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
