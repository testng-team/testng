package org.testng.internal;

import java.lang.reflect.Constructor;

import org.testng.IObjectFactory;
import org.testng.TestNGException;

/**
 * Default factory for test creation.
 * Note that if no constructor is found matching the specified parameters,
 * this factory will try to invoke a constructor that takes in a string object
 * 
 * @author Hani Suleiman
 *         Date: Mar 6, 2007
 *         Time: 12:00:27 PM
 * @since 5.6
 */
public class ObjectFactoryImpl implements IObjectFactory {
  
  public Object newInstance(Constructor constructor, Object... params) {
    Class[] paramClasses = new Class[params.length];
    for(int i = 0; i < params.length; i++) {
      paramClasses[i] = params[i].getClass();
    }
    try {
      return constructor.newInstance(params);
    }
    catch (IllegalAccessException ex) {
      return ClassHelper.tryOtherConstructor(constructor.getDeclaringClass());
    }
    catch (InstantiationException ex) {
      return ClassHelper.tryOtherConstructor(constructor.getDeclaringClass());
    }
    catch(Exception ex) {
      throw new TestNGException("Cannot instantiate class " + constructor.getDeclaringClass().getName(), ex);
    }
  }
}
