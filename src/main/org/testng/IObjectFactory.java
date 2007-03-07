package org.testng;

import java.lang.reflect.Constructor;

/**
 * Factory used to create all test instances.
 * @author Hani Suleiman
 *         Date: Mar 6, 2007
 *         Time: 11:57:24 AM
 */
public interface IObjectFactory {
  Object newInstance(Constructor constructor, Object... params);
}
