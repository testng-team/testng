package test.cli.github1517;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassWithConfigSkipSample {
  @BeforeClass
  public void beforeClass() {
    throw new SkipException("Simulating a configuration skip");
  }

  @Test
  public void testMethod() {
    assertThat(true).isTrue();
  }
}
