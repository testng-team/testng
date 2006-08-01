package test.superclass;

import org.testng.annotations.Test;

public class ChildSampleTest3 extends BaseSampleTest3 {
  @Test
  public void pass() {
      assert true;
  }

  @Test
  public void fail() {
      assert false;
  }
}
