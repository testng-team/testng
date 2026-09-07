package test.github3234;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A test class whose unused helper names {@link MissingType} in its signature. When that type is
 * not on the class loader, {@code Class#getDeclaredMethods} throws {@code NoClassDefFoundError}.
 */
public class SampleWithUnresolvedMethodType {

  @Test
  public void test1() {
    Assert.fail("should not be silently skipped");
  }

  private MissingType unused() {
    return null;
  }
}
