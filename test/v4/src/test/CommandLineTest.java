package test;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import test.sample.JUnitSample1;

import testhelper.OutputDirectoryPatch;

public class CommandLineTest {
  
  /**
   * Test -junit
   */
  @Test(groups = { "current" } )
  public void junitParsing() {
    String[] argv = {
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(), 
      "-junit", 
      "-testclass", "test.sample.JUnitSample1"  
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);
    
    List<ITestResult> passed = tla.getPassedTests();
    assertEquals(passed.size(), 2);
    String test1 = passed.get(0).getName();
    String test2 = passed.get(1).getName();
    
    assertTrue(JUnitSample1.EXPECTED1.equals(test1) && JUnitSample1.EXPECTED2.equals(test2) || 
        JUnitSample1.EXPECTED1.equals(test2) && JUnitSample1.EXPECTED2.equals(test1));
    }
  
  /**
   * Test the absence of -junit
   */
  @Test(groups = { "current" } )
  public void junitParsing2() {
    String[] argv = {
      "-log", "0",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-testclass", "test.sample.JUnitSample1"  
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);
    
    List<ITestResult> passed = tla.getPassedTests();
    assertEquals(passed.size(), 0);
    }
  
  private static void ppp(String s) {
    System.out.println("[CommandLineTest] " + s);
  }
  
}
