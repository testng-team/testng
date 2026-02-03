package test.listeners.factory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.CommandLineArgs;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.listeners.factory.issue3120.FactoryListenerTestClassCombinedSample;
import test.listeners.factory.issue3120.TestClassSample;

public class TestNGFactoryTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3059")
  public void testListenerFactoryViaConfigurationArg() {
    String[] args =
        new String[] {
          CommandLineArgs.LISTENER_FACTORY,
          SampleTestFactory.class.getName(),
          CommandLineArgs.TEST_CLASS,
          SampleTestCase.class.getName(),
          CommandLineArgs.LISTENER,
          ExampleListener.class.getName()
        };
    TestNG testng = TestNG.privateMain(args, null);
    assertThat(SampleTestFactory.instance).isNotNull();
    assertThat(ExampleListener.getInstance()).isNotNull();
    assertThat(testng.getStatus()).isZero();
  }

  @Test(description = "GITHUB-3059")
  public void testListenerFactoryViaTestNGApi() {
    TestNG testng = new TestNG();
    SampleTestFactory factory = new SampleTestFactory();
    testng.setListenerFactory(factory);
    testng.setListenerClasses(List.of(ExampleListener.class));
    testng.setTestClasses(new Class[] {SampleTestCase.class});
    testng.run();
    assertThat(testng.getStatus()).isZero();
    assertThat(factory.isInvoked()).isTrue();
  }

  @Test(description = "GITHUB-3120", dataProvider = "dp-3120")
  public void testListenerFactoryInvocationWhenCoupledAsListener(Class<?> testClass) {
    TestNG testng = create(testClass);
    AtomicInteger counter = new AtomicInteger(0);
    testng.addListener(
        new ITestListener() {
          @Override
          public void onTestFailure(ITestResult result) {
            counter.incrementAndGet();
          }
        });
    testng.run();
    assertThat(testng.getStatus()).isZero();
    assertThat(counter.get()).withFailMessage("No test should have failed").isEqualTo(0);
  }

  @DataProvider(name = "dp-3120")
  public Object[][] dp3120() {
    return new Object[][] {
      {TestClassSample.class}, {FactoryListenerTestClassCombinedSample.class},
    };
  }
}
