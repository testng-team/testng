package org.testng.internal.objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.testng.TestNGException;
import org.testng.internal.ClassHelper;

/** Utility class for object instantiations. */
public final class InstanceCreator {

  private static final String CANNOT_INSTANTIATE_CLASS = "Cannot instantiate class ";

  private InstanceCreator() {
    // Hide Constructor
  }

  public static <T> T newInstance(String className, Object... parameters) {
    Class<?> clazz = ClassHelper.forName(className);
    return (T) newInstance(clazz, parameters);
  }

  public static <T> T newInstance(Class<T> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (IllegalAccessException
        | InstantiationException
        | ExceptionInInitializerError
        | NoSuchMethodException
        | SecurityException e) {
      throw new TestNGException(CANNOT_INSTANTIATE_CLASS + clazz.getName(), e);
    } catch (InvocationTargetException e) {
      throw new TestNGException(CANNOT_INSTANTIATE_CLASS + clazz.getName(), e.getCause());
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

  public static <T> T newInstance(Class<T> cls, Object... parameters) {
    Constructor<T> ctor = null;
    for (Constructor<?> c : cls.getConstructors()) {
      // Just comparing parameter array sizes. Comparing the parameter types
      // is more error prone since we need to take conversions into account
      // (int -> Integer, etc...).
      if (c.getParameterTypes().length == parameters.length) {
        ctor = (Constructor<T>) c;
        break;
      }
    }
    if (ctor == null) {
      throw new TestNGException("Couldn't find a constructor in " + cls);
    }
    return newInstance(ctor, parameters);
  }
}
