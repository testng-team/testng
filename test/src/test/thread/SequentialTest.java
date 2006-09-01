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
    Helper.reset();
    
    String class1 = "test.thread.SequentialSampleTest";
    String class2 = "test.thread.SequentialSample2Test";
    String class3 = "test.thread.SequentialSample3Test";
    
    addClass(class1);
    addClass(class2);
    addClass(class3);
    setParallel(XmlSuite.PARALLEL_METHODS);
    setThreadCount(threadCount);

    run();
    
    Map<Long, Long>[] maps = new Map[] {
        Helper.getMap(class1),
        Helper.getMap(class2),
        Helper.getMap(class3),
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
