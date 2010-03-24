package test.verify;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Illustrate the implementation of a @Verify/@Verifier test.
 * 
 * One method should be annotated with @Verifier and then each test method
 * annotationed with @Verify will be followed with a call to the @Verifier
 * method.
 */
@Listeners(VerifyMethodInterceptor.class)
public class VerifyTest {

  private int m_invocationCount;

  @BeforeClass
  public void init() {
    m_invocationCount = 0;
  }

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

  @Test
  public void realVerify() {
    Assert.assertEquals(m_invocationCount, 4);
  }

  private void log(String string) {
    m_invocationCount++;
    System.out.println(string);
  }
}
