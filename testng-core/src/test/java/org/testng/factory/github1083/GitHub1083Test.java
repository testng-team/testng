package org.testng.factory.github1083;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.factory.samples.github1083.ArrayFactorySample;
import org.testng.factory.samples.github1083.ConstructorFactorySample;
import org.testng.factory.samples.github1083.DataProviderArrayFactorySample;
import org.testng.factory.samples.github1083.DataProviderInstanceInfoFactorySample;
import org.testng.factory.samples.github1083.InstanceInfoFactorySample;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class GitHub1083Test extends SimpleBaseTest {

  @Test(dataProvider = "dp", description = "GITHUB-1083")
  public void testArrayFactorySample(Class<?> sampleClass)
      throws NoSuchFieldException, IllegalAccessException {
    List<String> parameters = getParameters(sampleClass);
    parameters.clear();
    TestNG tng = create(sampleClass);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(parameters).containsExactly("bar");
    assertThat(listener.getSucceedMethodNames()).containsExactly("test");
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      new Object[] {ArrayFactorySample.class},
      new Object[] {ConstructorFactorySample.class},
      new Object[] {DataProviderArrayFactorySample.class},
      new Object[] {DataProviderInstanceInfoFactorySample.class},
      new Object[] {InstanceInfoFactorySample.class}
    };
  }

  public static List<String> getParameters(Class<?> clazz)
      throws NoSuchFieldException, IllegalAccessException {
    Field parameters = clazz.getField("parameters");
    return (List<String>) parameters.get(null);
  }
}
