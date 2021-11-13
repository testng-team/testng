package test.ignore;

import org.testng.annotations.Test;

public class ParentClassTestSample {

  @Test
  public void testa() {
    hook();
  }

  @Test
  public void testb() {
    hook();
  }

  @Test
  public void testc() {
    hook();
  }

  protected void hook() {}
}
