package test.dependent;

import org.testng.Assert;


public class DifferentClassDependsOnGroupsTest1 {
    /**
     * @testng.test groups = "mainGroup"
     */
    public void test0() {
      Assert.assertTrue(1 == 0); // Force a failure
    }
    
    /**
     * @testng.test dependsOnGroups="mainGroup"
     */
    public void test2() {
    }
}
