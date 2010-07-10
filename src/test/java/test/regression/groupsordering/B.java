package test.regression.groupsordering;


import org.testng.annotations.Test;

public class B extends Base {

  @Test(groups= "a")
  public void testB() throws Exception {
    Base.s_childBWasRun= true;
  }
}
