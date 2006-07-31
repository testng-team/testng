package test.dependsongroups;


public class TestFixture1 {
  /**
   * @testng.before-method groups="test,testgroup"
   */
  public void setup() {
    System.out.println("TestFixture setup");
  }
  
}
