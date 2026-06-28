package test.cli.github1517;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassWithConfigSkipAndFailureSample {
  @BeforeClass
  public void beforeClass() {
    throw new RuntimeException("Simulating a configuration failure");
  }

  @BeforeMethod
  public void beforeMethod() {
    // Intentionally left empty
  }

  @Test
  public void testMethod() {
    assertThat(true).isTrue();
  }
}
