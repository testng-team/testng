package test.configuration.issue3000;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassSample extends MyBaseTestSample {

  @BeforeClass
  public void beforeClass() {
    assertThat(dependency).isNotNull();
  }

  @Test
  public void test() {}
}
