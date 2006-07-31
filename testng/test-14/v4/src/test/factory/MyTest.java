package test.factory;



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

  /**
   * @testng.test groups="MyTest"
   */
  public void testMethod() {
    FactoryInSeparateClass.addToSum(i);
    //    assert i > 0 : "MyTest was not constructed with correct params";
    assert (i != 0) : "My test was not created by the factory";
  }

  /**
   * @testng.test dependsOnGroups="testMethodOnFactoryClass"
   */
  public void verifyThatTestMethodOnFactoryClassWasRun() {
    assert FactoryInSeparateClass.wasRun() : "Test method on factory class wasn't run";
  }

}