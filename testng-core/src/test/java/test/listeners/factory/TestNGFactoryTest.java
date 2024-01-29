package test.listeners.factory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.testng.CommandLineArgs;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

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
    assertThat(ExampleListener.instance).isNotNull();
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
}
