package test;

import java.util.List;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import test.sample.JUnitSample1;

import testhelper.OutputDirectoryPatch;

public class CommandLineTest {
  
  /**
   * Test -junit
   * 
   * @testng.test groups = "current"
   */
  public void junitParsing() {
    String[] argv = {
      "-sourcedir", "src",
      "-d", OutputDirectoryPatch.getOutputDirectory(),
      "-log", "0",
      "-junit", 
      "-testclass", "test.sample.JUnitSample1"  
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);
    
    List passed = tla.getPassedTests();
    assert passed.size() == 2;
    String test1 = ((ITestResult) passed.get(0)).getName();
    String test2 = ((ITestResult) passed.get(1)).getName();
    
    assert JUnitSample1.EXPECTED1.equals(test1) && JUnitSample1.EXPECTED2.equals(test2) || 
        JUnitSample1.EXPECTED1.equals(test2) && JUnitSample1.EXPECTED2.equals(test1);
    }
  
  /**
   * Test the absence of -junit
   * 
   * @testng.test groups = "current"
   */
  public void junitParsing2() {
    String[] argv = {
        "-sourcedir", "src",
        "-log", "0",
        "-d", OutputDirectoryPatch.getOutputDirectory(),
        "-testclass", "test.sample.JUnitSample1"  
    };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);
    
    List passed = tla.getPassedTests();
    assert passed.size() == 0;
    }
  
  private static void ppp(String s) {
    System.out.println("[CommandLineTest] " + s);
  }
  
}
