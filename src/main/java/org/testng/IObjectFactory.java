package org.testng;

import java.lang.reflect.Constructor;

/**
 * Factory used to create all test instances. This factory is passed the constructor
 * along with the parameters that TestNG calculated based on the environment
 * (@Parameters, etc...).
 *
 * @see IObjectFactory2
 *
 * @author Hani Suleiman
 * @since 5.6
 */
public interface IObjectFactory extends ITestObjectFactory {
  Object newInstance(Constructor constructor, Object... params);
}
