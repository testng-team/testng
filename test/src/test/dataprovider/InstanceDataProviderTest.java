package test.dataprovider;

import org.testng.annotations.Factory;


public class InstanceDataProviderTest {

  @Factory
  public Object[] create() {
    return new Object[] {
        new InstanceDataProviderSampleTest(),
        new InstanceDataProviderSampleTest()
    };
  }
}
