package test.skippingexceptions;

import org.testng.annotations.Test;

/**
 * This class tests @SkippingdExceptions
 *
 * @author gjuillot
 */
public class SampleExceptions {

  @Test(skippingExceptions = NumberFormatException.class )
  public void shouldSkip() {
    throw new NumberFormatException();
  }

  @Test(skippingExceptions = NumberFormatException.class)
  public void shouldFail() {
    throw new RuntimeException();
  }

  @Test(skippingExceptions = NumberFormatException.class)
  public void shouldPass1() {
  }

  @Test(expectedExceptions = NumberFormatException.class, skippingExceptions = NumberFormatException.class)
  public void shouldPass2() {
    throw new NumberFormatException();
  }
}
