package test.objectfactory;

import java.lang.reflect.Constructor;

import org.testng.internal.ObjectFactoryImpl;

/**
 * @author Hani Suleiman
 *         Date: Mar 6, 2007
 *         Time: 3:53:28 PM
 */
public class LoggingObjectFactory extends ObjectFactoryImpl
{
  public static int invoked;
  
  public Object newInstance(Constructor constructor, Object... params)
  {
    invoked++;
    return super.newInstance(constructor, params);
  }
}
