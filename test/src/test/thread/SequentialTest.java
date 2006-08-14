package test.thread;

import org.testng.Assert;
import org.testng.annotations.Test;

import test.BaseTest;

public class SequentialTest extends BaseTest {
  
  @Test
  public void verifySequential() {
    addClass("test.thread.SequentialSampleTest");
    setParallel(true);
    
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
