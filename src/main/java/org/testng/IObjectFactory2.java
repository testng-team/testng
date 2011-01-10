package org.testng;


/**
 * Factory used to create all test instances. This object factory only receives the class
 * in parameter.
 *
 * @see org.testng.IObjectFactory
 *
 * @author Cedric Beust <cedric@beust.com>
 *
 * @since 5.14.6
 */
public interface IObjectFactory2 extends ITestObjectFactory {
  Object newInstance(Class<?> cls);
}
