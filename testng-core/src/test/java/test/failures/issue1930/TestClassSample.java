package test.failures.issue1930;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample extends TestBase {

  private PrimeNumberChecker primeNumberChecker;

  @BeforeMethod
  public void initialize() {
    primeNumberChecker = new PrimeNumberChecker();
  }

  @DataProvider(name = "test1")
  public static Object[][] primeNumbers() {
    return new Object[][] {{2, true}, {6, false}, {19, true}, {22, true}, {23, false}};
  }

  @Test(dataProvider = "test1")
  public void testPrimeNumberChecker(int inputNumber, boolean expectedResult) {
    Assert.assertEquals(expectedResult, primeNumberChecker.validate(inputNumber));
  }
}
