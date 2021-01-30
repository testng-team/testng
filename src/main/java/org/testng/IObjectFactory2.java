package org.testng;

/**
 * Factory used to create all test instances. This object factory only receives the class in
 * parameter.
 *
 * @see org.testng.ITestObjectFactory
 * @since 5.14.6
 */
@Deprecated
public interface IObjectFactory2 extends ITestObjectFactory {

  @Deprecated
  default Object newInstance(Class<?> cls) {
    return newInstance(cls, new Object[0]);
  }
}
