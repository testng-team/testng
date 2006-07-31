package test.sample;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Cedric Beust, Apr 30, 2004
 */
public class BaseSampleInheritance {

   protected List m_configurations = new ArrayList();

   protected void addConfiguration(String c) {
      m_configurations.add(c);
   }

   protected boolean m_invokedBaseMethod = false;

   /**
    * @testng.test groups="inheritedTestMethod"
    */
   public void baseMethod() {
      m_invokedBaseMethod = true;
   }

   protected boolean m_invokedBaseConfiguration = false;

   /**
    * @testng.before-class
    */
   public void baseConfiguration() {
      m_invokedBaseConfiguration = true;
   }

   /**
    * @testng.before-class
    *                       dependsOnGroups="configuration0"
    *                       groups="configuration1"
    */
   public void configuration1() {
//    System.out.println("CONFIGURATION 1");
      addConfiguration("configuration1");
   }

   /**
    * @testng.test dependsOnGroups="inheritedTestMethod"
    */
   public void testBooleans() {
      assert m_invokedBaseMethod : "Didn't invoke test method in base class";
      assert m_invokedBaseConfiguration : "Didn't invoke configuration method in base class";
   }

}
