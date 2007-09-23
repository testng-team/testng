package test.invocationcount;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.internal.AnnotationTypeEnum;

public class FailedInvocationCountTest {
  
  /**
   * @testng.test
   */
  public void verifyAttributeShouldStop() {
    TestNG testng = new TestNG();
    testng.setVerbose(0);
    testng.setSourcePath(".");
    testng.setAnnotations(AnnotationTypeEnum.JAVADOC.toString());
    testng.setTestClasses(new Class[] { FailedInvocationCount2.class });
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), 8);
    Assert.assertEquals(tla.getFailedTests().size(), 7);
    Assert.assertEquals(tla.getSkippedTests().size(), 5);
    
  }
}
