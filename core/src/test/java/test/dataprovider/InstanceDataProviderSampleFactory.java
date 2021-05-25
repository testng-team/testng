package test.dataprovider;

import org.testng.annotations.Factory;

public class InstanceDataProviderSampleFactory {

  @Factory
  public Object[] create() {
    return new Object[] {new InstanceDataProviderSample(), new InstanceDataProviderSample()};
  }
}
