package test.expectedexceptions;

import org.testng.annotations.Test;

/**
 * This class tests @ExpectedExceptions
 *
 * @author cbeust
 */
public class SampleExceptions {

  @Test(expectedExceptions = {NumberFormatException.class})
  public void shouldPass() {
    throw new NumberFormatException();
  }

  @Test(expectedExceptions = {NumberFormatException.class})
  public void shouldFail1() {
    throw new RuntimeException();
  }

  @Test(expectedExceptions = {NumberFormatException.class})
  public void shouldFail2() {}

  @Test(expectedExceptions = RuntimeException.class)
  public void shouldFail3() {}
}
