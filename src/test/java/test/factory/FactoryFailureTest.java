package test.factory;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class FactoryFailureTest extends SimpleBaseTest {

   @Test
   public void factoryThrowingShouldNotRunTests() {
     TestNG tng = create(FactoryFailureSampleTest.class);

     try {
       tng.run();
       Assert.fail();
     }
     catch(Exception ex) {
       // success
     }
   }
}
