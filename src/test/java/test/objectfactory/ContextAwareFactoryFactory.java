package test.objectfactory;

import org.testng.IObjectFactory;
import org.testng.ITestContext;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.ObjectFactoryImpl;

/**
 * @author Hani Suleiman
 *         Date: Mar 8, 2007
 *         Time: 10:05:55 PM
 */
public class ContextAwareFactoryFactory
{
  @ObjectFactory
  public IObjectFactory create(ITestContext context) {
    assert context != null;
    return new ObjectFactoryImpl();
  }
}
