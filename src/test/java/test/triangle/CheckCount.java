package test.triangle;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * this test checks the CountCalls's static counter to see if we got the
 * expected number of calls
 */
public class CheckCount {

  @Parameters({ "expected-calls" })
  @Test
  public void testCheckCount(String expectedCalls) {
    int i = Integer.valueOf(expectedCalls);
    int numCalls =  CountCalls.getNumCalls();
    Assert.assertEquals(numCalls, i);
  }
}