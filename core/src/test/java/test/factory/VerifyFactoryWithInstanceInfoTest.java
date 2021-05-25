package test.factory;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class VerifyFactoryWithInstanceInfoTest {

  @Test(dependsOnGroups = {"first"})
  public void mainCheck() {
    List<Integer> numbers = FactoryWithInstanceInfo2Sample.getNumbers();
    Assert.assertTrue(numbers.contains(42), "Didn't find 42");
    Assert.assertTrue(numbers.contains(43), "Didn't find 43");
    Assert.assertEquals(numbers.size(), 2);
  }
}
