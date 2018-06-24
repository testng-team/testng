package org.testng;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DryRunSample {

  @BeforeSuite
  public void beforeSuite() {
    throw new RuntimeException("Error");
  }

  @BeforeClass
  public void beforeClass() {
    throw new RuntimeException("error");
  }

  @Test(dataProvider = "dp")
  public void test1(int i) {}

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
