package test.confordering.subpckg2;

import test.confordering.ConfigurationMethodOrderingTest;
import test.confordering.subpckg1.Parent;



public class NonOverriddingChild extends Parent {
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void childBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("childBeforeTestClass");
  }
  
  /**
   * @----testng.configuration beforeTestClass="true"
   */
  /*public void inheritBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("childInheritBeforeTestClass");
  }*/
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void childBeforeTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("childBeforeTestMethod");
  }
  
  /**
   * @testng.test
   */
  public void childTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("childTestMethod");
  }

  /**
   * @testng.configuration afterTestMethod="true"
   */
  public void childAfterTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("childAfterTestMethod");
  }
  

  /**
   * @testng.configuration afterTestClass="true"
   */
  public void childAfterTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("childAfterTestClass");
  }
  
}