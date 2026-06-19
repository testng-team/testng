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
    assertThat(PartialGroupTest.m_successMethod)
        .withFailMessage("test1 (method) should have been invoked")
        .isTrue();
    assertThat(PartialGroupTest.m_successClass)
        .withFailMessage("test2 (class) should have been invoked")
        .isTrue();
  }
}
