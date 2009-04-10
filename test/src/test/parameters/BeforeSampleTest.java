package test.parameters;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import junit.framework.Assert;

public class BeforeSampleTest {
   @BeforeMethod
   @Parameters("parameter")
   public static void beforeMethod(String parameter) {
     Assert.assertEquals("parameter value", parameter);
   }

   @DataProvider(name = "dataProvider")
   public static Object[][] dataProvider() {
     return new Object[][]{{"abc", "def"}};
   }

   @Test(dataProvider = "dataProvider")
   public static void testExample(String a, String b) {
     Assert.assertEquals("abc", a);
     Assert.assertEquals("def", b);
   }
}