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

  /**
   * @deprecated - This interface stands deprecated as of TestNG 7.5.0
   * @param cls - The class for which a new instance is to be created
   * @return - The newly created object.
   */
  @Deprecated
  default Object newInstance(Class<?> cls) {
    return newInstance(cls, new Object[0]);
  }
}
