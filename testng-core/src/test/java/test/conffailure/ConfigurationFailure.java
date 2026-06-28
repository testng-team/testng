package test.conffailure;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.testhelper.OutputDirectoryPatch;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.conffailure.github990.AbstractBaseSample;
import test.conffailure.github990.ChildClassSample;

/**
 * Test various cases where the @Configuration methods fail
 *
 * <p>Created on Jul 20, 2005
 *
 * @author cbeust
 */
public class ConfigurationFailure extends SimpleBaseTest {

  @Test
  public void beforeTestClassFails() {
    runTest(ClassWithFailedBeforeTestClass.class, ClassWithFailedBeforeTestClassVerification.class);
    assertThat(ClassWithFailedBeforeTestClassVerification.success())
        .withFailMessage("Not all the @Configuration methods of Run2 were run")
        .isTrue();
  }

  @Test
  public void beforeTestSuiteFails() {
    runTest(ClassWithFailedBeforeSuite.class, ClassWithFailedBeforeSuiteVerification.class);
    assertThat(ClassWithFailedBeforeSuiteVerification.success())
        .withFailMessage("No @Configuration methods should have run")
        .isTrue();
  }

  private static void runTest(Class<?>... classes) {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create(classes);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.addListener(tla);
    testng.run();
  }

  @Test(description = "GITHUB-990")
  public void ensureConfigurationRunsFromBaseClass() {
    TestNG testng = create(ChildClassSample.class);
    testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    testng.run();
    assertThat(AbstractBaseSample.messages).containsExactly("cleanup");
  }
}
