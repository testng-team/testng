package test.hook.issue2251;

import org.testng.annotations.Test;

public class SampleTestCase extends AbstractBaseTestCase {

  static class NullExObj {

    @Override
    public String toString() {
      throw new NullPointerException("expected");
    }
  }

  @Test(timeOut = 1000000) // removing timeout fixes error output
  public void testError() throws Exception {
    new NullExObj().toString();
  }
}
