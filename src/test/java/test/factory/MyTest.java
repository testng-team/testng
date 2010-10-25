package test.factory;

import org.testng.annotations.Test;

public class MyTest {
  private int i;

  // in this test, our default constructor sets s to a value that will cause a failure
  // the valid test instances should come from the factory
  public MyTest() {
    i = 0;
  }

  public MyTest(int i) {
    this.i = i;
  }

  @Test(groups = "MyTest")
  public void testMethod() {
    FactoryInSeparateClass.addToSum(i);
    //    assert i > 0 : "MyTest was not constructed with correct params";
    assert (i != 0) : "My test was not created by the factory";
  }

  @Test(dependsOnGroups = "testMethodOnFactoryClass")
  public void verifyThatTestMethodOnFactoryClassWasRun() {
    assert FactoryInSeparateClass.wasRun() : "Test method on factory class wasn't run";
  }

}