package org.testng;

/**
 * Factory used to create all test instances. This factory is passed the constructor along with the
 * parameters that TestNG calculated based on the environment (@Parameters, etc...).
 *
 * @see IObjectFactory2
 * @since 5.6
 * @deprecated - This interface stands deprecated as of TestNG 7.5.0
 */
@Deprecated
public interface IObjectFactory extends ITestObjectFactory {}
