package org.testng.internal.dynamicgraph;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryTestClassSample {
  private String text;

  @Factory(dataProvider = "getData")
  public FactoryTestClassSample(String text) {
    this.text = text;
  }

  @Test
  public void testMethod() {}

  @Test
  public void anotherTestMethod() {}

  @Override
  public String toString() {
    return text;
  }

  @DataProvider(name = "getData")
  public static Object[][] getData() {
    return new Object[][] {{"one"}, {"two"}, {"three"}};
  }
}
