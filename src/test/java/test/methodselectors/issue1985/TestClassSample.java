package test.methodselectors.issue1985;

import org.testng.annotations.Test;

public class TestClassSample {

  @Test(groups = {"bat"})
  public void batTest() {
  }

  @Test(groups = {"p1"})
  public void p1Test() {
  }

  @Test(groups = {"p2"})
  public void p2Test() {
  }

  @Test(groups = {"bat", "p3"})
  public void batp3Test() {
  }
}
