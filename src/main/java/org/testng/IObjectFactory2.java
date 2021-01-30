package org.testng;

/**
 * Factory used to create all test instances. This object factory only receives the class in
 * parameter.
 *
 * @see org.testng.ITestObjectFactory
 * @since 5.14.6
 * @deprecated - This interface stands deprecated as of TestNG 7.5.0
 */
@Deprecated
public interface IObjectFactory2 extends ITestObjectFactory {

  @Deprecated
  /**
   * @deprecated - This interface stands deprecated as of TestNG 7.5.0
   */
  default Object newInstance(Class<?> cls) {
    return newInstance(cls, new Object[0]);
  }
}
