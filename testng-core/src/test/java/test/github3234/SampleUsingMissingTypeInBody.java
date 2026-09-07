package test.github3234;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test class that only names {@link MissingType} inside a method body. Method inspection
 * succeeds; the missing type surfaces when the test runs.
 */
public class SampleUsingMissingTypeInBody {

  @Test
  public void test1() {
    new MissingType();
    Assert.fail("should not be reached");
  }
}
