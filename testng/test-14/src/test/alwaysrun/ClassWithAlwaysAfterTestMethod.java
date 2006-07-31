package test.alwaysrun;


public class ClassWithAlwaysAfterTestMethod {
  
  /**
   * @testng.before-method
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
   * @testng.after-method alwaysRun="true"
   */
  public void afterTestMethod() {
    AlwaysRunTest.LOG.append("-after");
  }

}
