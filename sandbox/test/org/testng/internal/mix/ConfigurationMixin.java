package org.testng.internal.mix;


import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * This class/interface
 */
public class ConfigurationMixin {
  @BeforeClass
  public void beforeClass() {
    System.out.println("beforeClass");
  }

  @AfterClass
  public void afterClass() {
    System.out.println("afterClass");
  }

  @BeforeMethod
  public void beforeMethod() {
    System.out.println("beforeMethod");
  }

  @AfterMethod
  public void afterMethod() {
    System.out.println("afterMethod");
  }
}
