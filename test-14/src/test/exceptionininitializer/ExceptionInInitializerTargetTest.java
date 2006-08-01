package test.exceptionininitializer;


public class ExceptionInInitializerTargetTest {
  private static final String s_exceptionTriggerField= InnerClass.generateException();
  
  /**
   * @testng.test
   */
  public void fakeTestMethod() {
  }

  
  private static class InnerClass {

    public static String generateException() {
      if(true) {
        throw new RuntimeException("exception in static field initializer");
        
      }
      return null;
    }
  }
}
