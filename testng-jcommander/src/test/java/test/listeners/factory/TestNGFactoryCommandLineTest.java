package test.listeners.factory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.cli.CliOptions;

/**
 * The command line half of {@code test.listeners.factory.TestNGFactoryTest}, which stays in {@code
 * testng-core} for the case that drives the Java API.
 */
public class TestNGFactoryCommandLineTest {

  @Test(description = "GITHUB-3059")
  public void testListenerFactoryViaConfigurationArg() {
    String[] args =
        new String[] {
          CliOptions.LISTENER_FACTORY,
          SampleTestFactory.class.getName(),
          CliOptions.TEST_CLASS,
          SampleTestCase.class.getName(),
          CliOptions.LISTENER,
          ExampleListener.class.getName()
        };
    TestNG testng = TestNG.privateMain(args, null);
    assertThat(SampleTestFactory.instance).isNotNull();
    assertThat(ExampleListener.getInstance()).isNotNull();
    assertThat(testng.getStatus()).isZero();
  }
}
