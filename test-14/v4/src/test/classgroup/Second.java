package test.classgroup;



/**
 * @testng.test dependsOnGroups="first"
 */
public class Second {

  /**
   * @tesng.test
   */
  public void verify() {
    assert First.allRun() : "Methods for class First should have been invoked first.";
  }
  
}