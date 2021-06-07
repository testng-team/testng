package org.testng.internal.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import org.testng.IClass;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IParametersAnnotation;
import org.testng.internal.Parameters;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;
import org.testng.internal.objects.pojo.DetailedAttributes;
import org.testng.util.Strings;
import org.testng.xml.XmlTest;

/** A plain vanilla Object dispenser */
class SimpleObjectDispenser implements IObjectDispenser {

  private final ITestObjectFactory objectFactory;

  SimpleObjectDispenser(ITestObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  @Override
  public void setNextDispenser(IObjectDispenser dispenser) {
    // We are not going to be doing anything with this downstream dispenser since we are
    // last in the chain of responsibility.
    throw new UnsupportedOperationException(
        "Cannot allow adding any further downstream object dispensers.");
  }

  @Override
  public Object dispense(CreationAttributes attributes) {
    DetailedAttributes detailed = attributes.getDetailedAttributes();
    if (detailed != null) {
      return createInstance(
          detailed.getDeclaringClass(),
          detailed.getClasses(),
          detailed.getXmlTest(),
          detailed.getFinder(),
          objectFactory,
          detailed.isCreate(),
          detailed.getErrorMsgPrefix());
    }
    BasicAttributes basic = attributes.getBasicAttributes();
    if (basic == null) {
      throw new TestNGException("Encountered problems in creating instances.");
    }
    if (basic.getRawClass() == null) {
      try {
        return objectFactory.newInstance(basic.getTestClass().getRealClass());
      } catch (TestNGException e) {
        return null;
      }
    }
    return objectFactory.newInstance(basic.getRawClass());
  }

  /* Create an instance for the given class. */
  static <T> T createInstance(
      Class<T> declaringClass,
      Map<Class<?>, IClass> classes,
      XmlTest xmlTest,
      IAnnotationFinder finder,
      ITestObjectFactory objectFactory,
      boolean create,
      String errorMsgPrefix) {
    T result = null;

    try {
      Constructor<T> constructor = findAnnotatedConstructor(finder, declaringClass);
      if (null != constructor) {
        // Any annotated constructor?
        try {
          result =
              instantiateUsingParameterizedConstructor(finder, constructor, xmlTest, objectFactory);
        } catch (IllegalArgumentException e) {
          return null;
        }
      } else {
        // No, just try to instantiate the parameterless constructor (or the one with a String)
        result =
            instantiateUsingDefaultConstructor(declaringClass, classes, xmlTest, objectFactory);
      }
    } catch (TestNGException ex) {
      throw ex;
    } catch (NoSuchMethodException ex) {
      // Empty catch block
    } catch (Throwable cause) {
      // Something else went wrong when running the constructor
      throw new TestNGException(
          "An error occurred while instantiating class "
              + declaringClass.getName()
              + ": "
              + cause.getMessage(),
          cause);
    }

    if (result == null && create) {
      String suffix = "instantiated";
      if (!Modifier.isPublic(declaringClass.getModifiers())) {
        suffix += "/accessed.";
      }
      if (Strings.isNotNullAndNotEmpty(errorMsgPrefix)) {
        suffix = suffix + ". Root cause: " + errorMsgPrefix;
      }
      throw new TestNGException(
          "An error occurred while instantiating class "
              + declaringClass.getName()
              + ". "
              + "Check to make sure it can be "
              + suffix);
    }

    return result;
  }

  private static <T> T instantiateUsingParameterizedConstructor(
      IAnnotationFinder finder,
      Constructor<T> constructor,
      XmlTest xmlTest,
      ITestObjectFactory objectFactory) {
    IFactoryAnnotation factoryAnnotation =
        finder.findAnnotation(constructor, IFactoryAnnotation.class);
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

  private static <T> T instantiateUsingDefaultConstructor(
      Class<T> declaringClass,
      Map<Class<?>, IClass> classes,
      XmlTest xmlTest,
      ITestObjectFactory factory)
      throws NoSuchMethodException {
    // If this class is a (non-static) nested class, the constructor contains a hidden
    // parameter of the type of the enclosing class
    Class<?>[] parameterTypes = new Class[0];
    Object[] parameters = new Object[0];
    Class<?> ec = declaringClass.getEnclosingClass();
    boolean isStatic = 0 != (declaringClass.getModifiers() & Modifier.STATIC);

    // Only add the extra parameter if the nested class is not static
    if ((null != ec) && !isStatic) {
      parameterTypes = new Class[] {ec};
      parameters = new Object[] {computeParameters(classes, ec, factory)};
    } // isStatic

    Constructor<T> ct;
    try {
      ct = declaringClass.getDeclaredConstructor(parameterTypes);
    } catch (NoSuchMethodException ex) {
      ct = declaringClass.getDeclaredConstructor(String.class);
      parameters = new Object[] {xmlTest.getName()};
    }
    ct.setAccessible(true);
    return factory.newInstance(ct, parameters);
  }

  private static Object computeParameters(
      Map<Class<?>, IClass> classes, Class<?> ec, ITestObjectFactory factory)
      throws NoSuchMethodException {
    // Create an instance of the enclosing class so we can instantiate
    // the nested class (actually, we reuse the existing instance).
    IClass enclosingIClass = classes.get(ec);
    if (enclosingIClass == null) {
      return factory.newInstance(ec);
    }
    Object[] enclosingInstances = enclosingIClass.getInstances(false);
    if (enclosingInstances == null || enclosingInstances.length == 0) {
      return factory.newInstance(ec.getConstructor(ec));
    }
    return enclosingInstances[0];
  }

  /** Find the best constructor given the parameters found on the annotation */
  private static <T> Constructor<T> findAnnotatedConstructor(
      IAnnotationFinder finder, Class<T> declaringClass) {
    Constructor<T>[] constructors = (Constructor<T>[]) declaringClass.getDeclaredConstructors();

    for (Constructor<T> result : constructors) {
      IParametersAnnotation parametersAnnotation =
          finder.findAnnotation(result, IParametersAnnotation.class);
      if (parametersAnnotation != null) {
        String[] parameters = parametersAnnotation.getValue();
        Class<?>[] parameterTypes = result.getParameterTypes();
        if (parameters.length != parameterTypes.length) {
          throw new TestNGException(
              "Parameter count mismatch:  "
                  + result
                  + "\naccepts "
                  + parameterTypes.length
                  + " parameters but the @Test annotation declares "
                  + parameters.length);
        }
        return result;
      }

      IFactoryAnnotation factoryAnnotation =
          finder.findAnnotation(result, IFactoryAnnotation.class);
      if (factoryAnnotation != null) {
        return result;
      }
    }

    return null;
  }
}
