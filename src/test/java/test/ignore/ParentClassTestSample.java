package test.ignore;

import org.testng.annotations.Test;

public class ParentClassTestSample {
  @Test
  public void testc() {
    hook();
  }

  protected void hook() {};
}
