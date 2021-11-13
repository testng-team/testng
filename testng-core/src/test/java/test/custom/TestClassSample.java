package test.custom;

import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;

public class TestClassSample {

  @Test(
      attributes = {
        @CustomAttribute(
            name = "joy",
            values = {"KingFisher", "Bira"})
      })
  public void testMethod() {}
}
