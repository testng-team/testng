package test.parameters;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

public class SuiteSampleTest {

  @Parameters({"param"})
  @BeforeSuite
  public void suiteParameter(String s) {
    ppp("PARAM:" + s);
  }

  private void ppp(String s) {
    System.out.println("[SuiteSampleTest] " + s);
  }
}
