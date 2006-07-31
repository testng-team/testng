package test.abstractbaseclass;

import org.testng.Assert;

public class DummyTest extends SetUp
{
   public DummyTest()
   {
   }

   /**
    * @testng.test
    */
   public void test()
   {
//     ppp("ASSERTING SETUP " + hashCode());
     Assert.assertTrue(m_setUp);
   }
   
   private void ppp(String string) {
     System.out.println("[DummyTest] " + string);
   }

}