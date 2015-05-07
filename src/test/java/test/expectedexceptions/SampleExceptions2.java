package test.expectedexceptions;

import org.testng.annotations.Test;

/**
 * This class tests @ExpectedExceptions
 *
 * @author cbeust
 */
public class SampleExceptions2 {

  @Test(expectedExceptions = NumberFormatException.class )
  public void shouldPass() {
    throw new NumberFormatException();
  }

  @Test(expectedExceptions = NumberFormatException.class)
  public void shouldFail1() {
    throw new RuntimeException();
  }

  @Test(expectedExceptions = NumberFormatException.class)
  public void shouldFail2() {
  }

  @Test(expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = ".*bomb.*")
  public void shouldPass2() {
    throw new NumberFormatException("This should not bomb at all");
  }

  @Test(expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = ".*bombc.*")
  public void shouldFail3() {
    throw new NumberFormatException("This should bomb for good");
  }

  @Test(expectedExceptions = NumberFormatException.class, expectedExceptionsMessageRegExp = ".*")
  public void shouldPass3() {
    throw new NumberFormatException(null);
  }

  @Test(expectedExceptions = NumberFormatException.class, expectedExceptionsMessageRegExp = "Multiline.*")
  public void shouldPass4() {
    throw new NumberFormatException("Multiline\nException");
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void shouldFail4() {
  }
}
