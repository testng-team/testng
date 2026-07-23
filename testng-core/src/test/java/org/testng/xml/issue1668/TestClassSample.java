package org.testng.xml.issue1668;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class TestClassSample {
  @Test
  public void testMethod() {
    assertThat(true).isTrue();
  }
}
