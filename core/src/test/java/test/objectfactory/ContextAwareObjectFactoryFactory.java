package test.objectfactory;

import org.testng.ITestContext;
import org.testng.ITestObjectFactory;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.objects.ObjectFactoryImpl;

public class ContextAwareObjectFactoryFactory {

  public static int invoked;

  @ObjectFactory
  public ITestObjectFactory create(ITestContext context) {
    assert context != null;
    invoked++;
    return new ObjectFactoryImpl();
  }
}
