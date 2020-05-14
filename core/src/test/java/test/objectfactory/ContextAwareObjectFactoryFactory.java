package test.objectfactory;

import org.testng.IObjectFactory;
import org.testng.ITestContext;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.ObjectFactoryImpl;

public class ContextAwareObjectFactoryFactory {

  @ObjectFactory
  public IObjectFactory create(ITestContext context) {
    assert context != null;
    return new ObjectFactoryImpl();
  }
}
