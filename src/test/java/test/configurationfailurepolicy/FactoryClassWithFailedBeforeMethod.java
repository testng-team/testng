package test.configurationfailurepolicy;

import org.testng.annotations.Factory;

public class FactoryClassWithFailedBeforeMethod extends ClassWithFailedBeforeMethod {
  @Factory
  public Object[] createTests() {
    Object[] instances = new Object[2];
    instances[0] = new FactoryClassWithFailedBeforeMethod();
    instances[1] = new FactoryClassWithFailedBeforeMethod();
    return instances;
  }
}
