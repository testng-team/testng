package test.conffailure;

import static org.testng.Assert.assertTrue;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import testhelper.OutputDirectoryPatch;

/**
 * Test various cases where the @Configuration methods fail
 *
 * Created on Jul 20, 2005
 * @author cbeust
 */
public class ConfigurationFailure {

  @Test
  public void beforeTestClassFails() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] {
        ClassWithFailedBeforeTestClass.class,
        ClassWithFailedBeforeTestClassVerification.class
    });
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.run();
    assertTrue(ClassWithFailedBeforeTestClassVerification.success(),
        "Not all the @Configuration methods of Run2 were run");
  }

  @Test
  public void beforeTestSuiteFails() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] { ClassWithFailedBeforeSuite.class, ClassWithFailedBeforeSuiteVerification.class });
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.run();
    assertTrue(ClassWithFailedBeforeSuiteVerification.success(),
        "No @Configuration methods should have run");
  }

  private static void ppp(String s) {
    System.out.println("[AlwaysRunTest] " + s);
  }
}
