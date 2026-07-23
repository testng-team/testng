package test.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * This class verifies the PartialGroupTest
 *
 * @author cbeust
 */
public class PartialGroupVerification {
  @Test
  public void verify() {
    assertThat(PartialGroupTest.m_successMethod && PartialGroupTest.m_successClass)
        .withFailMessage("test1 and test2 should have been invoked both")
        .isTrue();
  }
}
