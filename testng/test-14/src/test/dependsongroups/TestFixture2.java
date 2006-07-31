package test.dependsongroups;


public class TestFixture2 {
  /**
   * @testng.before-method groups="test" dependsOnGroups="testgroup"
   */
  public void setup() {
    System.out.println("TestFixture2 setup");
  }

  /**
   * @testng.test groups="test"
   */
  public void testMethod() {
    System.out.println("test method");
  }
}
