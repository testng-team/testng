package test.factory;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.internal.AnnotationTypeEnum;

public class FactoryWithDataProviderTest {

  /**
   * @testng.test
   */
  public void verifyDataProvider() {
    TestNG tng = new TestNG();
    tng.setSourcePath("./test-14/src;src");
//    tng.setVerbose(0);
    tng.setAnnotations(AnnotationTypeEnum.JAVADOC.toString());
    tng.setTestClasses(new Class[] { FactoryWithDataProvider.class });
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), 4);
    
  }

  private static void ppp(String s) {
    System.out.println("[FactoryWithDataProviderTest] " + s);
  }
}
