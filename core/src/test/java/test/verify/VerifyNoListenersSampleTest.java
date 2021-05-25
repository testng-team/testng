package test.verify;

import org.testng.annotations.Test;

/**
 * Same class as VerifySampleTest but without the @Listeners annotation.
 */
public class VerifyNoListenersSampleTest {

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
}
