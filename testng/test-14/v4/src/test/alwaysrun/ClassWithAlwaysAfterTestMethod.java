package test.alwaysrun;


public class ClassWithAlwaysAfterTestMethod {
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void beforeTestMethod() {
    AlwaysRunTest.LOG.append("-before");
  }

  /**
   * @testng.test
   */
  public void nonFailingTest() {
    AlwaysRunTest.LOG.append("-nonfail");
  }

  /**
   * @testng.test dependsOnMethods="nonFailingTest"
   */
  public void failingTest() {
    AlwaysRunTest.LOG.append("-fail");
    throw new RuntimeException();
  }

  /**
   * @testng.configuration alwaysRun="true" afterTestMethod="true"
   */
  public void afterTestMethod() {
    AlwaysRunTest.LOG.append("-after");
  }

}
