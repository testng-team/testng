package test.thread;

import org.testng.Assert;
import org.testng.xml.XmlSuite;

import test.BaseTest;

public class SequentialTest extends BaseTest {
  
  /**
   * @testng.test
   */
  public void verifySequential() {
    addClass("test.thread.SequentialSampleTest");
    setParallel(XmlSuite.PARALLEL_METHODS);
    
    run();
    
//    String[] passed = {
//        "f1", "f2", "f3"
//    };
//    String[] failed = {
//    };
//    String[] skipped = {
//    };
//    verifyTests("Passed", passed, getPassedTests());
//    verifyTests("Failed", failed, getFailedTests());
//    verifyTests("Skipped", skipped, getSkippedTests());
//    
    Assert.assertEquals(SequentialSampleTest.m_threads.size(), 1);
  }

}
