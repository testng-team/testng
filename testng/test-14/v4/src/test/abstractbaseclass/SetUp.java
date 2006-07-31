package test.abstractbaseclass;

import junit.framework.TestCase;

public class SetUp extends TestCase {
  protected boolean m_setUp = false;
  
       public SetUp()
       {
       }

   /**
    * this is the suites common setUp-method
    *
    * @testng.configuration beforeSuite = "true"
    */
   public void setUp()
   {
//     ppp("SETTING SETUP TO TRUE " + hashCode());
     m_setUp = true;
   }

  private void ppp(String string) {
    System.out.println("[SetUp] " + string);
  }
   
}