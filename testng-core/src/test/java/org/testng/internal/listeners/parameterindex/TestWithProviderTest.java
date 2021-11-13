package org.testng.internal.listeners.parameterindex;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ParameterIndexTestListener.class)
public class TestWithProviderTest {

  @DataProvider
  public Object[] integerValues() {
    return new Object[] {1, 4, 4};
  }

  @Test(dataProvider = "integerValues")
  public void ShouldBeLessThan5(int value) {
    assertTrue(value < 5);
  }
}
