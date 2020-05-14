package test.objectfactory;

import org.testng.IObjectFactory;
import org.testng.annotations.ObjectFactory;

public class MyObjectFactoryFactory {

  @ObjectFactory
  public IObjectFactory create() {
    return new LoggingObjectFactory();
  }
}
