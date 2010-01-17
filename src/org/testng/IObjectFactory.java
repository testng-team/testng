package org.testng;

import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 * Factory used to create all test instances.
 * @author Hani Suleiman
 * @since 5.6
 */
public interface IObjectFactory extends Serializable{
  Object newInstance(Constructor constructor, Object... params);
}
