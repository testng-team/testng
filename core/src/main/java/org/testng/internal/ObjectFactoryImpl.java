package org.testng.internal;

import org.testng.IObjectFactory;
import org.testng.TestNGException;

import java.lang.reflect.Constructor;

/**
 * Default factory for test creation. Note that if no constructor is found matching the specified
 * parameters, this factory will try to invoke a constructor that takes in a string object
 *
 * @since 5.6
 */
public class ObjectFactoryImpl implements IObjectFactory {

  @Override
  public Object newInstance(Constructor constructor, Object... params) {
    if (constructor == null) {
      throw new IllegalArgumentException("Constructor cannot be null.");
    }
    try {
      constructor.setAccessible(true);
      return constructor.newInstance(params);
    } catch (IllegalAccessException | InstantiationException ex) {
      return ClassHelper.tryOtherConstructor(constructor.getDeclaringClass());
    } catch (SecurityException e) {
      throw new TestNGException(constructor.getName() + " must be public", e);
    } catch (Exception ex) {
      throw new TestNGException(
          "Cannot instantiate class " + constructor.getDeclaringClass().getName(), ex);
    }
  }
}
