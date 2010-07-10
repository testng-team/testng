package org.testng;

import java.util.Collection;

import org.testng.annotations.Test;


/**
 * This class/interface 
 */
public class AssertTest {
  @Test
  public void nullObjectArrayAssertEquals() {
    Object[] expected= null;
    Object[] actual= null;
    Assert.assertEquals(actual, expected);
  }
  
  @Test
  public void nullObjectArrayAssertNoOrder() {
    Object[] expected= null;
    Object[] actual= null;
    Assert.assertEqualsNoOrder(actual, expected);
  }
  
  @Test
  public void nullCollectionAssertEquals() {
    Collection expected = null;
    Collection actual = null;
    Assert.assertEquals(actual, expected);
  }
}
