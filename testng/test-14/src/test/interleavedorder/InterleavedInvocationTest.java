package test.interleavedorder;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import test.BaseTest;
import testhelper.OutputDirectoryPatch;

public class InterleavedInvocationTest extends BaseTest {
  public static final StringBuffer LOG= new StringBuffer();
  
  /**
   * @testng.test
   */
  public void invocationOrder() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class[] { TestChild1.class, TestChild2.class });
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.run();
    
    final String log= LOG.toString();
    final String clsName= TestChild1.class.getName();
    
    Assert.assertTrue(LOG.indexOf(clsName + ".beforeTestChildOneClass "
        + clsName + ".testOne "
        + clsName + ".testTwo "
        + clsName + ".testThree "
        + clsName + ".afterTestChildOneClass") > -1);
  }

}
