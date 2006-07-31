package test.alwaysrun;

import org.testng.Assert;
import org.testng.TestNG;

import testhelper.OutputDirectoryPatch;

public class AlwaysRunTest {
  static StringBuffer LOG;
  
  /**
   * @testng.test
   */
  public void checkAlwaysRunTestMethod() {
    LOG = new StringBuffer();
    TestNG runner = new TestNG();
    runner.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    runner.setVerbose(1);
    runner.setTestClasses(new Class[] {ClassWithAlwaysAfterTestMethod.class});
    runner.run();
    
    Assert.assertEquals(LOG.toString(), "-before-nonfail-after-before-fail-after", "all methods should have been invoked");
  }
  
  /**
   * @testng.test
   */
  public void checkAlwaysRunClassMethod() {
    LOG = new StringBuffer();
    TestNG runner = new TestNG();
    runner.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    runner.setVerbose(1);
    runner.setTestClasses(new Class[] {ClassWithAlwaysAfterClassMethod.class});
    runner.run();
    
    Assert.assertEquals(LOG.toString(), "-before-nonfail-fail-after", "all methods should have been invoked");
  }
  
  /**
   * @testng.test
   */
  public void checkWithInnerClass() {
    LOG = new StringBuffer();
    TestNG runner = new TestNG();
    runner.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    runner.setVerbose(1);
    runner.setTestClasses(new Class[] {SimpleInnerClass.class});
    runner.run();
    
    Assert.assertEquals(LOG.toString(), "dummy", "inner class should have been run");
  }
  
  static class SimpleInnerClass {
    /**
     * @testng.test
     */
    public void dummyTest() {
      LOG.append("dummy");
    }
  }
}
