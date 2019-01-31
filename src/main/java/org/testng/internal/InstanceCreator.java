package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import org.testng.IClass;
import org.testng.IMethodSelector;
import org.testng.IObjectFactory;
import org.testng.IObjectFactory2;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IParametersAnnotation;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.util.Strings;
import org.testng.xml.XmlTest;

/** Utility class for object instantiations. */
public final class InstanceCreator {

  private static final String CANNOT_INSTANTIATE_CLASS = "Cannot instantiate class ";

  private InstanceCreator() {
    // Hide Constructor
  }

  public static <T> T newInstance(Class<T> clazz) {
    try {
      return clazz.newInstance();
    } catch (IllegalAccessException
        | InstantiationException
        | ExceptionInInitializerError
        | SecurityException
        | NullPointerException e) {
      throw new TestNGException(CANNOT_INSTANTIATE_CLASS + clazz.getName(), e);
    }
  }

  public static <T> T newInstanceOrNull(Class<T> clazz) {
    try {
      Constructor<T> constructor = clazz.getConstructor();
      return newInstance(constructor);
    } catch (ExceptionInInitializerError | SecurityException e) {
      throw new TestNGException(CANNOT_INSTANTIATE_CLASS + clazz.getName(), e);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  public static <T> T newInstance(Constructor<T> constructor, Object... parameters) {
    try {
      return constructor.newInstance(parameters);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new TestNGException(
          CANNOT_INSTANTIATE_CLASS + constructor.getDeclaringClass().getName(), e);
    }
  }

  public static IMethodSelector createSelector(org.testng.xml.XmlMethodSelector selector) {
    try {
      Class<?> cls = Class.forName(selector.getClassName());
      return (IMethodSelector) cls.newInstance();
    } catch (Exception ex) {
      throw new TestNGException("Couldn't find method selector : " + selector.getClassName(), ex);
    }
  }

  /** Create an instance for the given class. */
  public static Object createInstance(
      Class<?> declaringClass,
      Map<Class<?>, IClass> classes,
      XmlTest xmlTest,
      IAnnotationFinder finder,
      ITestObjectFactory objectFactory,
      boolean create,
      String errorMsgPrefix) {
    if (objectFactory instanceof IObjectFactory) {
      return createInstanceUsingObjectFactory(
          declaringClass, classes, xmlTest, finder, (IObjectFactory) objectFactory, create, errorMsgPrefix);
    } else if (objectFactory instanceof IObjectFactory2) {
      return createInstanceUsingObjectFactory(declaringClass, (IObjectFactory2) objectFactory);
    } else {
      throw new AssertionError("Unknown object factory type:" + objectFactory);
    }
  }

  private static Object createInstanceUsingObjectFactory(Class<?> declaringClass, IObjectFactory2 objectFactory) {
    return objectFactory.newInstance(declaringClass);
  }

  public static Object createInstanceUsingObjectFactory(
      Class<?> declaringClass,
      Map<Class<?>, IClass> classes,
      XmlTest xmlTest,
      IAnnotationFinder finder,
      IObjectFactory factory,
      boolean create,
      String errorMsgPrefix) {
    Object result = null;

    try {
      Constructor<?> constructor = ClassHelper.findAnnotatedConstructor(finder, declaringClass);
      if (null != constructor) {
      // Any annotated constructor?
        try {
          result = instantiateUsingParameterizedConstructor(finder, constructor, xmlTest, factory);
        } catch(IllegalArgumentException e) {
          return null;
        }
      } else {
        // No, just try to instantiate the parameterless constructor (or the one with a String)
        result = instantiateUsingDefaultConstructor(declaringClass, classes, xmlTest, factory);
      }
    } catch (TestNGException ex) {
      throw ex;
    } catch (NoSuchMethodException ex) {
      // Empty catch block
    } catch (Throwable cause) {
      // Something else went wrong when running the constructor
      throw new TestNGException(
          "An error occurred while instantiating class "
              + declaringClass.getName() + ": " + cause.getMessage(), cause);
    }

    if (result == null && create) {
      String suffix = "instantiated";
      if (!Modifier.isPublic(declaringClass.getModifiers())) {
        suffix += "/accessed.";
      }
      if (Strings.isNotNullAndNotEmpty(errorMsgPrefix)) {
        suffix = suffix + ". Root cause: " + errorMsgPrefix;
      }
      throw new TestNGException("An error occurred while instantiating class " + declaringClass.getName() + ". "
              + "Check to make sure it can be " + suffix);
    }

    return result;
  }

  private static Object instantiateUsingParameterizedConstructor(IAnnotationFinder finder,
      Constructor<?> constructor, XmlTest xmlTest, IObjectFactory objectFactory) {
    IFactoryAnnotation factoryAnnotation = finder
        .findAnnotation(constructor, IFactoryAnnotation.class);
    if (factoryAnnotation != null) {
      throw new IllegalArgumentException("No factory annotation found.");
    }

    IParametersAnnotation parametersAnnotation =
        finder.findAnnotation(constructor, IParametersAnnotation.class);
    if (parametersAnnotation == null) {
      // null if the annotation is @Factory
      return null;
    }
    String[] parameterNames = parametersAnnotation.getValue();
    Object[] parameters =
        Parameters.createInstantiationParameters(
            constructor,
            "@Parameters",
            finder,
            parameterNames,
            xmlTest.getAllParameters(),
            xmlTest.getSuite());
    return objectFactory.newInstance(constructor, parameters);
  }


  private static Object instantiateUsingDefaultConstructor(Class<?> declaringClass,
      Map<Class<?>, IClass> classes, XmlTest xmlTest, IObjectFactory factory)
      throws NoSuchMethodException, IllegalAccessException, InstantiationException {
    // If this class is a (non-static) nested class, the constructor contains a hidden
    // parameter of the type of the enclosing class
    Class<?>[] parameterTypes = new Class[0];
    Object[] parameters = new Object[0];
    Class<?> ec = declaringClass.getEnclosingClass();
    boolean isStatic = 0 != (declaringClass.getModifiers() & Modifier.STATIC);

    // Only add the extra parameter if the nested class is not static
    if ((null != ec) && !isStatic) {
      parameterTypes = new Class[] {ec};
      parameters = new Object[]{computeParameters(classes, ec, factory)};
    } // isStatic

    Constructor<?> ct;
    try {
      ct = declaringClass.getDeclaredConstructor(parameterTypes);
    } catch (NoSuchMethodException ex) {
      ct = declaringClass.getDeclaredConstructor(String.class);
      parameters = new Object[]{xmlTest.getName()};
      // If ct == null here, we'll pass a null
      // constructor to the factory and hope it can deal with it
    }
    return factory.newInstance(ct, parameters);
  }

  private static Object computeParameters(Map<Class<?>, IClass> classes,
      Class<?> ec, IObjectFactory factory)
      throws NoSuchMethodException, IllegalAccessException, InstantiationException {
    // Create an instance of the enclosing class so we can instantiate
    // the nested class (actually, we reuse the existing instance).
    IClass enclosingIClass = classes.get(ec);
    if (enclosingIClass == null) {
      return ec.newInstance();
    }
    Object[] enclosingInstances = enclosingIClass.getInstances(false);
    if (enclosingInstances == null || enclosingInstances.length == 0) {
      return factory.newInstance(ec.getConstructor(ec));
    }
    return enclosingInstances[0];
  }

}
