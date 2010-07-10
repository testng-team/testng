package test.morten;

import org.testng.annotations.Factory;

public class SampleTestFactory {
  public SampleTestFactory() {} // CTR necessary ?
  @Factory public Object[] createInstances() {
  return new SampleTest[] {
    new SampleTest(1, 0.1f),
    new SampleTest(10, 0.5f),
  };
  }
};
