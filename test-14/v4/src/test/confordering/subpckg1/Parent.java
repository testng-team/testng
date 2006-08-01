package test.confordering.subpckg1;

import test.confordering.ConfigurationMethodOrderingTest;


public class Parent {
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void parentBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentBeforeTestClass");
  }
  
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void inheritBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentInheritBeforeTestClass");
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void parentBeforeTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentBeforeTestMethod");
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void inheritBeforeTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentInheritBeforeTestMethod");
  }
  
  /**
   * @testng.test
   */
  public void parentTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentTestMethod");
  }

  /**
   * @testng.configuration afterTestMethod="true"
   */
  public void parentAfterTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentAfterTestMethod");
  }
  
  /**
   * @testng.configuration afterTestMethod="true"
   */
  public void inheritAfterTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentInheritAfterTestMethod");
  }

  /**
   * @testng.configuration afterTestClass="true"
   */
  public void parentAfterTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentAfterTestClass");
  }
  
  /**
   * @testng.configuration afterTestClass="true"
   */
  public void inheritAfterTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentInheritAfterTestClass");
  }
}