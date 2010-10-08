package test.objectfactory;

import org.testng.annotations.ObjectFactory;

/**
 * @author Hani Suleiman
 *         Date: Mar 6, 2007
 *         Time: 5:03:19 PM
 */
public class BadMethodFactoryFactory
{
  @ObjectFactory
  public Object create() {
    return new LoggingObjectFactory();
  }
}
