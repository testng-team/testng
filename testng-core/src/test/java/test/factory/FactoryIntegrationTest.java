package test.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.ITestClassInstance;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.factory.issue3111.SimpleFactoryPoweredTestSample;
import test.factory.issue3111.SimpleFactoryPoweredTestWithIndicesSample;
import test.factory.issue3111.SimpleFactoryPoweredTestWithoutDataProviderSample;
import test.factory.issue3111.SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample;

public class FactoryIntegrationTest extends SimpleBaseTest {

  @Test(description = "https://github.com/cbeust/testng/issues/876")
  public void testExceptionWithNonStaticFactoryMethod() {
    TestNG tng = create(GitHub876Sample.class);
    try {
      tng.run();
      failBecauseExceptionWasNotThrown(TestNGException.class);
    } catch (TestNGException e) {
      assertThat(e)
          .hasMessage(
              "\nCan't invoke public java.lang.Object[] test.factory.GitHub876Sample"
                  + ".createInstances(): either make it static or add a no-args constructor to "
                  + "your class");
    }
  }

  @Test
  public void testNonPublicFactoryMethodShouldWork() {
    TestNG tng = create(NonPublicFactory.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 2);
  }

  @Test
  public void testExceptionWithBadFactoryMethodReturnType() {
    TestNG tng = create(BadMethodReturnTypeFactory.class);
    try {
      tng.run();
      failBecauseExceptionWasNotThrown(TestNGException.class);
    } catch (TestNGException e) {
      assertThat(e)
          .hasMessage(
              "\ntest.factory.BadMethodReturnTypeFactory.createInstances MUST return [ java.lang"
                  + ".Object[] or org.testng.IInstanceInfo[] ] but returns java.lang.Object");
    }
  }

  @Test
  public void doubleFactoryMethodShouldWork() {
    TestNG tng = create(DoubleFactory.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    // TODO containsExactly is not used here because the order is not consistent. Check if we should
    // fix it.
    assertThat(listener.getSucceedMethodNames())
        .contains(
            "FactoryBaseSample{1}#f",
            "FactoryBaseSample{2}#f", "FactoryBaseSample{3}#f", "FactoryBaseSample{4}#f");
  }

  @Test(dataProvider = "testdata", description = "GITHUB-3111")
  public void ensureCurrentIndexWorksForFactoryPoweredTests(Class<?> klass, Integer[] expected) {
    List<ITestClassInstance> params = new ArrayList<>();
    TestNG testng = create(klass);
    testng.addListener(
        new ITestListener() {
          @Override
          public void onTestSuccess(ITestResult result) {
            params.add(result.getMethod().getFactoryMethodParamsInfo());
          }
        });
    testng.run();
    List<Integer> actualIndices =
        params.stream()
            .map(ITestClassInstance::getInvocationIndex)
            .sorted()
            .collect(Collectors.toList());
    assertThat(actualIndices).containsExactly(expected);
  }

  @DataProvider(name = "testdata")
  public Object[][] testdata() {
    return new Object[][] {
      {SimpleFactoryPoweredTestSample.class, new Integer[] {0, 1, 2}},
      {SimpleFactoryPoweredTestWithIndicesSample.class, new Integer[] {0}},
      {SimpleFactoryPoweredTestWithoutDataProviderSample.class, new Integer[] {0, 1, 2}},
      {SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample.class, new Integer[] {0}},
    };
  }
}
