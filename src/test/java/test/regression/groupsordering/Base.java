package test.regression.groupsordering;


import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

public abstract class Base {
  protected static boolean s_childAWasRun;
  protected static boolean s_childBWasRun;

  @BeforeGroups(value= "a", groups= "a")
  public void setUp() throws Exception {
//    System.out.println("class is " + getClass().getName() + " Before group  ");
    Assert.assertFalse(s_childAWasRun || s_childBWasRun, "Static field was not reset: @AfterGroup method not invoked");
  }

  @AfterGroups(value= "a", groups= "a")
  public void tearDown() {
//    System.out.println("class is " + getClass().getName() + " After group  ");
    Assert.assertTrue(s_childAWasRun, "Child A was not run");
    Assert.assertTrue(s_childBWasRun, "Child B was not run");
    s_childAWasRun = false;
    s_childBWasRun = false;
  }

}
