package test.confordering.subpckg2;

import test.confordering.ConfigurationMethodOrderingTest;
import test.confordering.subpckg1.Parent;


/**
 * This child overrides some of the @Configuration methods defined in Parent
 * and also declares them as @Configuration
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class OverriddingChild extends Parent {
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void childBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("childBeforeTestClass");
  }
  
  /**
   * @testng.configuration beforeTestClass="true"
   */
  public void inheritBeforeTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("childInheritBeforeTestClass");
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void childBeforeTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("childBeforeTestMethod");
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void inheritBeforeTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("childInheritBeforeTestMethod");
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
   * @testng.configuration afterTestMethod="true"
   */
  public void inheritAfterTestMethod() {
    ConfigurationMethodOrderingTest.LOG.add("childInheritAfterTestMethod");
  }

  /**
   * @testng.configuration afterTestClass="true"
   */
  public void childAfterTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("childAfterTestClass");
  }
  
  /**
   * @testng.configuration afterTestClass="true"
   */
  public void inheritAfterTestClass() {
    ConfigurationMethodOrderingTest.LOG.add("childInheritAfterTestClass");
  }
}