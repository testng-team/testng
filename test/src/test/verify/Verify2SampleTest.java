package test.verify;

import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Illustrate the implementation of a @Verify/@Verifier test.
 * 
 * One method should be annotated with @Verifier and then each test method
 * annotated with @Verify will be followed with a call to the @Verifier
 * method.
 */
@Listeners(VerifyTestListener.class)
public class Verify2SampleTest implements ITestNGListenerFactory {

  @Verify
  @Test
  public void f1() {
    log("f1");  
  }

  @Verify
  @Test
  public void f2() {
    log("f2");  
  }

  @Verifier
  @Test
  public void verify() {
    log("Verifying");
  }

  private void log(String string) {
    if (false) {
      System.out.println(string);
    }
  }

  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    System.out.println("Creating a listener of type " + listenerClass);
    return null;
  }
}
