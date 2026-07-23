package test.dependent.issue141;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class MultipleMatchesTestClassSample {

  @Test(dependsOnMethods = "test_C[0-9]{7}")
  public void randomTest() {}

  @Test
  public void test_C6390323() {
    fail();
  }

  @Test
  public void test_C6390324() {}
}
