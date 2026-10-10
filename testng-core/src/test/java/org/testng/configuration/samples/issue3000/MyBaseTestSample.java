package org.testng.configuration.samples.issue3000;

import org.testng.annotations.BeforeClass;

abstract class MyBaseTestSample implements MyInterface {
  protected Object dependency;

  @Override
  public void setDependency(Object ignored) {}

  @BeforeClass
  public void setupDependency() {
    dependency = new Object();
  }

  // The "__" in this name is not in the sample from the GitHub issue. It was added so that TestNG
  // found this method first, which is the order that fails without the fix. That order comes
  // from a hash of the class and method names, so a rename or a move changes it.
  // BeforeClassTest therefore also sorts these methods from every starting order.
  @BeforeClass(dependsOnMethods = "setupDependency")
  public void __setupAdditionalDependency_() {}
}
