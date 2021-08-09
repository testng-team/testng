package test.factory;

import org.testng.annotations.Test;
import test.configuration.ConfigurationBaseTest;
import test.factory.sample.Factory2Sample;
import test.factory.sample.Factory2TestSample;

public class Factory2Test extends ConfigurationBaseTest {
  @Test
  public void testFactoryCorrectlyInterleaved() {
    testConfiguration(Factory2Sample.class, Factory2TestSample.class);
  }
}
