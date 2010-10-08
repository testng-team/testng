package test.objectfactory;

import org.testng.internal.ObjectFactoryImpl;

import java.lang.reflect.Constructor;

/**
 * @author Hani Suleiman
 *         Date: Mar 6, 2007
 *         Time: 3:53:28 PM
 */
public class LoggingObjectFactory extends ObjectFactoryImpl
{
  /**
   *
   */
  private static final long serialVersionUID = -395096650866727480L;
  public static int invoked;

  @Override
  public Object newInstance(Constructor constructor, Object... params)
  {
    invoked++;
    return super.newInstance(constructor, params);
  }
}
