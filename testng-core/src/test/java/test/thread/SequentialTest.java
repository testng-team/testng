package test.thread;

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
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

  @Test
  public void verifySingleThreaded1() {
    verifySingleThreaded(1);
  }

  @Test
  public void verifySingleThreaded2() {
    verifySingleThreaded(2);
  }

  @Test
  public void verifySingleThreaded3() {
    verifySingleThreaded(3);
  }

  public void verifySequential(int threadCount) {
    runTest(
        threadCount,
        SequentialSampleTest.class.getName(),
        SequentialSample2Test.class.getName(),
        SequentialSample3Test.class.getName());
  }

  public void verifySingleThreaded(int threadCount) {
    runTest(
        threadCount,
        SingleThreadedSampleTest.class.getName(),
        SingleThreadedSample2Test.class.getName(),
        SingleThreadedSample3Test.class.getName());
  }

  private void runTest(int threadCount, String... classes) {
    Helper.reset();

    for (String c : classes) {
      addClass(c);
    }
    setParallel(XmlSuite.ParallelMode.METHODS);
    setThreadCount(threadCount);

    run();

    Map<Long, Long>[] maps =
        new Map[] {
          Helper.getMap(classes[0]), Helper.getMap(classes[1]), Helper.getMap(classes[2]),
        };

    for (Map m : maps) {
      Assert.assertEquals(m.size(), 1);
    }

    long[] ids =
        new long[] {
          maps[0].keySet().iterator().next(),
          maps[1].keySet().iterator().next(),
          maps[2].keySet().iterator().next(),
        };
    Map<Long, Long> verifyMap = new HashMap<>();

    for (long id : ids) {
      verifyMap.put(id, id);
    }

    Assert.assertEquals(verifyMap.size(), threadCount);

    debug("COUNT:" + threadCount + " THREAD ID'S:" + ids[0] + " " + ids[1] + " " + ids[2]);
  }

  private void debug(String s) {
    Logger.getLogger(getClass()).debug("[SequentialTest] " + s);
  }
}
