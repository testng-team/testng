package test.factory.classconf;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class XClassOrderWithFactoryTest {
  @Test
  public void testBeforeAfterClassInvocationsWithFactory() {
    TestNG testng= new TestNG();
    testng.setTestClasses(new Class[] {XClassOrderWithFactory.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.run();
    Assert.assertEquals(XClassOrderWithFactory.LOG.toString(), XClassOrderWithFactory.EXPECTED_LOG);
  }
}
