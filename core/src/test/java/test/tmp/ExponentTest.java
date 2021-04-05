package test.tmp;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(suiteName = "Exponent suite", testName = "Exponent test")
public class ExponentTest {

    @DataProvider(name = "random")
    public Object[][] generateRandomExps() {
      // This array should be generated with random numbers
      return new Object[][] {
        new Object[] { 0.0, Math.exp(0) },
        new Object[] { 1.0, Math.exp(1) },
        new Object[] { 2.0, Math.exp(2) },
      };
    }

    @BeforeMethod
    public void setUp() {
      ppp("BEFORE METHOD");
    }

    @Test(dataProvider = "random")
    public void testExponent(double exponent, double expected) {
      ppp("COMPARING " + myExpFunction(exponent) + " AND " + expected);
      assertEquals(myExpFunction(exponent), expected);
    }

    private static void ppp(String s) {
      System.out.println("[ExponentTest] " + s);
    }

    private double myExpFunction(double exponent) {
      return Math.exp(exponent);
    }

}
