package test.tmp;

import org.testng.annotations.Test;

@Test
public class A {
  
  @Test(expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = ".*bomb.*")
  public void shouldPass2() {
    throw new NumberFormatException("This should bomb for good");
  }

  @Test(expectedExceptions = NumberFormatException.class,
      expectedExceptionsMessageRegExp = ".*bombc.*")
  public void shouldFail2() {
    throw new NumberFormatException("This should bomb for good");
  }
  
  @Test(expectedExceptions = NumberFormatException.class, expectedExceptionsMessageRegExp = ".*")
  public void shouldPass3() {
    throw new NumberFormatException(null);
  }
}
