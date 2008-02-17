package test.dataprovider;

import org.testng.annotations.Test;

public class InheritanceBTest extends InheritanceATest {

  @Test(dataProvider = "dp")
  public void f(String s) {
  }
}
