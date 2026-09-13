package org.testng.factory.github1631;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.factory.samples.github1631.DataProviderTransformer;
import org.testng.factory.samples.github1631.ExternalDataProviders;
import org.testng.factory.samples.github1631.FactoryWithExternalDataProviderTests;
import org.testng.factory.samples.github1631.FactoryWithLocalDataProviderTests;
import test.SimpleBaseTest;

public class GitHub1631Tests extends SimpleBaseTest {

  @Test(
      description =
          "GITHUB-1631: Test @Factory(dataProvider) should implicitly inject data provider class name")
  public void factoryWithLocalDataProviderShouldUseHostClassName() {
    final DataProviderTransformer transformer = runTest(FactoryWithLocalDataProviderTests.class);
    assertThat(transformer.getDataProviderClass())
        .isEqualTo(FactoryWithLocalDataProviderTests.class);
  }

  @Test(
      description =
          "GITHUB-1631: Test @Factory(dataProvider) should explicitly set data provider class name")
  public void factoryWithExplicitDataProviderShouldUseExternalClassName() {
    final DataProviderTransformer transformer = runTest(FactoryWithExternalDataProviderTests.class);
    assertThat(transformer.getDataProviderClass()).isEqualTo(ExternalDataProviders.class);
  }

  private DataProviderTransformer runTest(final Class<?> cls) {
    final TestNG tng = create(cls);
    final DataProviderTransformer dpt = new DataProviderTransformer();
    tng.addListener(dpt);
    tng.run();

    return dpt;
  }
}
