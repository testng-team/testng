
package test.sample;

import org.testng.annotations.Test;

/**
 * This class verifies the PartialGroupTest
 *
 * @author cbeust
 */

public class PartialGroupVerification {
  @Test
  public void verify() {
    assert PartialGroupTest.m_successMethod && PartialGroupTest.m_successClass
      : "test1 and test2 should have been invoked both";
  }
}