package test.factory.issue1770;

import org.testng.Reporter;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class SampleTestFour extends SampleTestBase {

  @Factory
  @Parameters({"isCustom"})
  public SampleTestFour(String fl) {
    super(fl);
  }

  @Test
  public void test() {
    Reporter.log(getFlag());
  }
}
