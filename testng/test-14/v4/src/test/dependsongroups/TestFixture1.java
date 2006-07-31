package test.dependsongroups;


public class TestFixture1 {
  /**
   * @testng.configuration beforeTestMethod="true" groups="test,testgroup"
   */
  public void setup() {
    System.out.println("TestFixture setup");
  }
  
}
