package test.abstractconfmethod;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class C extends B
{
   @Override
  @BeforeMethod
   public void testSetup() {}

   @Test(description="Test depends on a config method that has implements an abstract methods")
   public void test1() {}
}
