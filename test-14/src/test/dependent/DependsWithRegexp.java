package test.dependent;

import org.testng.AssertJUnit;

public class DependsWithRegexp
{
  private boolean m_1 = false;
  private boolean m_2 = false;
   /**
    * @testng.test groups="start"
    */
   public void start()
   {
       AssertJUnit.assertFalse(m_1);
       AssertJUnit.assertFalse(m_2);
       m_1 = true;
   }



   /**
    * @testng.test dependsOnGroups="start.*" groups="stop"
    */
   public void stop()
   {
     AssertJUnit.assertTrue(m_1);
     AssertJUnit.assertFalse(m_2);
     m_2 = true;
   }
   
   /**
    * @testng.after-test
    *
    */
   public void verify() {
     AssertJUnit.assertTrue(m_1);
     AssertJUnit.assertTrue(m_2);     
   }

}