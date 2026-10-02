package org.testng.factory;

import org.testng.annotations.Test;
import org.testng.configuration.samples.ConfigurationBaseTest;
import org.testng.factory.samples.Factory2TestSample;

public class Factory2Test extends ConfigurationBaseTest {
  @Test
  public void testFactoryCorrectlyInterleaved() {
    testConfiguration(Factory2TestSample.class);
  }
}
