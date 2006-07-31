package test.alwaysrun;


public class ClassWithAlwaysAfterClassMethod {
  
  /**
   * @testng.configuration beforeTestClass="true"
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
   * @testng.configuration alwaysRun="true" afterTestClass="true"
   */
  public void afterTestMethod() {
    AlwaysRunTest.LOG.append("-after");
  }

}
