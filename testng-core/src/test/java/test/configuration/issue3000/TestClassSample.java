package test.configuration.issue3000;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassSample extends MyBaseTestSample {

  @BeforeClass
  public void beforeClass() {
    assertNotNull(dependency);
  }

  @Test
  public void test() {}
}
