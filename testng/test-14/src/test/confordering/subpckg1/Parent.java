package test.confordering.subpckg1;

import test.confordering.ConfigurationMethodOrderingTest;


public class Parent {
  /**
   * @testng.before-class
   */
  public void parentBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentBeforeTestClass");
  }
  
  /**
   * @testng.before-class
   */
  public void inheritBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentInheritBeforeTestClass");
  }
  
  /**
   * @testng.before-method
   */
  public void parentBeforeTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentBeforeTestMethod");
  }
  
  /**
   * @testng.before-method
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
   * @testng.after-method
   */
  public void parentAfterTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentAfterTestMethod");
  }
  
  /**
   * @testng.after-method
   */
  public void inheritAfterTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("parentInheritAfterTestMethod");
  }

  /**
   * @testng.after-class
   */
  public void parentAfterTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentAfterTestClass");
  }
  
  /**
   * @testng.after-class
   */
  public void inheritAfterTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("parentInheritAfterTestClass");
  }
}