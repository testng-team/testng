package org.testng.factory;

import org.testng.annotations.Test;
import org.testng.factory.samples.Factory2TestSample;
import test.configuration.ConfigurationBaseTest;

public class Factory2Test extends ConfigurationBaseTest {
  @Test
  public void testFactoryCorrectlyInterleaved() {
    testConfiguration(Factory2TestSample.class);
  }
}
