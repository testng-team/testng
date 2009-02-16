package test.asserttests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AssertTest {

  @Test
  public void noOrderSuccess() {
    String[] rto1 = { "boolean", "BigInteger", "List",};
    String[] rto2 = {  "List", "BigInteger", "boolean",};
    Assert.assertEqualsNoOrder(rto1, rto2);
  }
  
  @Test(expectedExceptions = AssertionError.class)
  public void noOrderFailure() {
    String[] rto1 = { "a", "a", "b",};
    String[] rto2 = {  "a", "b", "b",};
    Assert.assertEqualsNoOrder(rto1, rto2);
  }
}
