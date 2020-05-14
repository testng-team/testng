package test.objectfactory;

import org.testng.annotations.ObjectFactory;

public class BadMethodObjectFactoryFactory {

  @ObjectFactory
  public Object create() {
    return new LoggingObjectFactory();
  }
}
