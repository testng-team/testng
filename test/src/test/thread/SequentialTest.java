package test.thread;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.BaseTest;

public class SequentialTest extends BaseTest {
  
  @Test
  public void verifySequential1() {
    verifySequential(1);
  }    

  @Test
  public void verifySequential2() {
    verifySequential(2);
  }
  
  @Test
  public void verifySequential3() {
    verifySequential(3);
  }    

  public void verifySequential(int threadCount) {
    runTest(threadCount, "test.thread.SequentialSampleTest",
        "test.thread.SequentialSample2Test", "test.thread.SequentialSample3Test");
  }
  
  private void runTest(int threadCount, String... classes) {
    Helper.reset();

    for (String c : classes) {
      addClass(c);
    }
    setParallel(XmlSuite.PARALLEL_METHODS);
    setThreadCount(threadCount);

    run();
    
    Map<Long, Long>[] maps = new Map[] {
        Helper.getMap(classes[0]),
        Helper.getMap(classes[1]),
        Helper.getMap(classes[2]),
    };

    for(Map m : maps) {
      Assert.assertEquals(m.size(), 1);
    }
    
    long[] ids = new long[] {
        maps[0].keySet().iterator().next().longValue(),
        maps[1].keySet().iterator().next().longValue(),
        maps[2].keySet().iterator().next().longValue(),
    };
    Map<Long, Long> verifyMap = new HashMap<Long, Long>();
    
    for (long id : ids) {
      verifyMap.put(id, id);
    }
    
    Assert.assertEquals(verifyMap.size(), threadCount);
    
    ppp("COUNT:" + threadCount  + " THREAD ID'S:" + ids[0] + " " + ids[1] + " " + ids[2]);
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[SequentialTest] " + s);
    }
  }

}
