package test.configuration.issue3000;

import org.testng.annotations.BeforeClass;

abstract class MyBaseTestSample implements MyInterface {
  protected Object dependency;

  public void setDependency(Object ignored) {}

  @BeforeClass
  public void setupDependency() {
    dependency = new Object();
  }

  // Had to add the "__" to this method (This is not how it looks like in the sample provided
  // in the GitHub issue). A combination of the fully qualified method names is what causes
  // this method to be first found in the configuration methods by TestNG and so it causes
  // the issue.
  @BeforeClass(dependsOnMethods = "setupDependency")
  public void __setupAdditionalDependency_() {}
}
