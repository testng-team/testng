package test.cli.github1517;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassWithConfigFailureSample {
  @BeforeClass
  public void beforeClass() {
    throw new RuntimeException("Simulating a configuration failure");
  }

  @Test
  public void testMethod() {
    assertThat(true).isTrue();
  }
}
