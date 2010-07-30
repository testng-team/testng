package test.configurationfailurepolicy;

import org.testng.annotations.Factory;

public class FactoryClassWithFailedBeforeClassMethod extends ClassWithFailedBeforeClassMethod {
  @Factory
  public Object[] createTests() {
    Object[] instances = new Object[2];
    instances[0] = new FactoryClassWithFailedBeforeClassMethod();
    instances[1] = new FactoryClassWithFailedBeforeClassMethod();
    return instances;
  }
}
