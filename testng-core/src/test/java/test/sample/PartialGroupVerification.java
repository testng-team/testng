package test.sample;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/**
 * This class verifies the PartialGroupTest
 *
 * @author cbeust
 */
public class PartialGroupVerification {
  @Test
  public void verify() {
    assertTrue(
        PartialGroupTest.m_successMethod && PartialGroupTest.m_successClass,
        "test1 and test2 should have been invoked both");
  }
}
