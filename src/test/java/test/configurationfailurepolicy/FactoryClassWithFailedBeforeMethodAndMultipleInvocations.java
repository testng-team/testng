package test.configurationfailurepolicy;

import org.testng.annotations.Factory;

public class FactoryClassWithFailedBeforeMethodAndMultipleInvocations extends ClassWithFailedBeforeMethodAndMultipleInvocations {
  @Factory
  public Object[] createTests() {
    Object[] instances = new Object[2];
    instances[0] = new FactoryClassWithFailedBeforeMethodAndMultipleInvocations();
    instances[1] = new FactoryClassWithFailedBeforeMethodAndMultipleInvocations();
    return instances;
  }
}
