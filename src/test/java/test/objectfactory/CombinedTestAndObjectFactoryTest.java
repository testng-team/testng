package test.objectfactory;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.BaseTest;

public class CombinedTestAndObjectFactoryTest extends BaseTest {
  @Test
  void combinedTestAndObjectFactory() {
    addClass(CombinedTestAndObjectFactorySample.class.getName());
    run();
    verifyTests("Should have passed", new String[]{"isConfigured"}, getPassedTests());
    verifyTests("Failures", new String[0], getFailedTests());
    verifyTests("Skipped", new String[0], getSkippedTests());
  }

  public static void main(String[] args) {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] {CombinedTestAndObjectFactorySample.class});
    tng.setVerbose(10);
    tng.run();
  }
}
