package test;

import test.sample.BaseSampleInheritance;

public class SampleInheritance extends BaseSampleInheritance {
   // Test dependency of configuration methods
   /**
    * @testng.configuration beforeTestClass="true" groups="configuration0"
    */
   public void configuration0() {
      addConfiguration("configuration0");
//    System.out.println("CONFIGURATION 0");
   }

   /**
    * @testng.configuration beforeTestClass="true"
    *                       dependsOnGroups="configuration1"
    */
   public void configuration2() {
      assert m_configurations.size() == 2
            : "Expected size 2 found " + m_configurations.size();
      assert "configuration0".equals(m_configurations.get(0))
            : "Expected configuration0 to be run";
      assert "configuration1".equals(m_configurations.get(1))
            : "Expected configuration1 to be run";
      addConfiguration("configuration2");
      assert m_configurations.size() == 3
            : "after configuration2() should be 3. Found: " + m_configurations.size();
   }

   /**
    * @testng.test dependsOnGroups="inheritedTestMethod"
    */
   public void inheritedMethodsWereCalledInOrder() {
      assert m_invokedBaseMethod : "Didn't invoke test method in base class";
      assert m_invokedBaseConfiguration : "Didn't invoke configuration method in base class";

   }

   /**
    * @testng.test
    */
   public void configurationsWereCalledInOrder() {
      assert m_configurations.size() == 3
            : "Expected 3 configurations. Found only " + m_configurations.size();
      assert "configuration0".equals(m_configurations.get(0))
            : "Expected configuration0 to be run";
      assert "configuration1".equals(m_configurations.get(1))
            : "Expected configuration1 to be run";
      assert "configuration2".equals(m_configurations.get(2))
            : "Expected configuration1 to be run";
   }
}
