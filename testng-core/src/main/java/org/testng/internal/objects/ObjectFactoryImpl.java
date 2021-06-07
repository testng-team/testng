package org.testng.internal.objects;

import java.lang.reflect.Constructor;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;

/**
 * Default factory for test creation. Note that if no constructor is found matching the specified
 * parameters, this factory will try to invoke a constructor that takes in a string object
 *
 * @since 5.6
 */
public class ObjectFactoryImpl implements ITestObjectFactory {

  @Override
  public <T> T newInstance(Constructor<T> constructor, Object... params) {
    if (constructor == null) {
      throw new IllegalArgumentException("Constructor cannot be null.");
    }
    try {
      constructor.setAccessible(true);
      return InstanceCreator.newInstance(constructor, params);
    } catch (TestNGException ex) {
      return tryOtherConstructor(constructor.getDeclaringClass());
    } catch (SecurityException e) {
      throw new TestNGException(constructor.getName() + " must be public", e);
    } catch (Exception ex) {
      throw new TestNGException(
          "Cannot instantiate class " + constructor.getDeclaringClass().getName(), ex);
    }
  }

  private static <T> T tryOtherConstructor(Class<T> declaringClass) {
    T result;
    try {
      // Special case for inner classes
      if (declaringClass.getModifiers() == 0) {
        return null;
      }

      Constructor<T> ctor = declaringClass.getConstructor(String.class);
      result = InstanceCreator.newInstance(ctor, "Default test name");
    } catch (Exception e) {
      String message = e.getMessage();
      if ((message == null) && (e.getCause() != null)) {
        message = e.getCause().getMessage();
      }
      String error =
          "Could not create an instance of class "
              + declaringClass
              + ((message != null) ? (": " + message) : "")
              + ".\nPlease make sure it has a constructor that accepts either a String or no parameter.";
      throw new TestNGException(error);
    }

    return result;
  }
}
