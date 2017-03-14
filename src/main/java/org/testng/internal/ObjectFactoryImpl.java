package org.testng.internal;

import org.testng.IObjectFactory;
import org.testng.TestNGException;

import java.lang.reflect.Constructor;

/**
 * Default factory for test creation.
 * Note that if no constructor is found matching the specified parameters,
 * this factory will try to invoke a constructor that takes in a string object
 *
 * @since 5.6
 */
public class ObjectFactoryImpl implements IObjectFactory {

  private static final long serialVersionUID = -4547389328475540017L;

  @Override
  public Object newInstance(Constructor constructor, Object... params) {
    try {
      constructor.setAccessible(true);
      return constructor.newInstance(params);
    } catch (IllegalAccessException | InstantiationException ex) {
      return ClassHelper.tryOtherConstructor(constructor.getDeclaringClass());
    } catch (SecurityException e) {
      throw new TestNGException(constructor.getName() + " must be public", e);
    } catch(Exception ex) {
      throw new TestNGException("Cannot instantiate class "
          + (constructor != null
                ? constructor.getDeclaringClass().getName()
                : ": couldn't find a suitable constructor"),
                    ex);
    }
  }
}
