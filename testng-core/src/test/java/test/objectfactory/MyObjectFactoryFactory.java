package test.objectfactory;

import org.testng.ITestObjectFactory;
import org.testng.annotations.ObjectFactory;

public class MyObjectFactoryFactory {

  @ObjectFactory
  public ITestObjectFactory create() {
    return new LoggingObjectFactory();
  }
}
