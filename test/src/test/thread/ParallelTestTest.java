package test.thread;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseTest;

public class ParallelTestTest extends BaseTest {
    
  private void createTest(XmlSuite xmlSuite, String className) {
    XmlTest result = new XmlTest(xmlSuite);
    List<XmlClass> classes = result.getXmlClasses();
    XmlClass xmlClass = new XmlClass(className);
    classes.add(xmlClass);
  }
  
  @Test
  public void verifySequential() {
    int threadCount = 2;
    String class1 = "test.thread.Test1Test";
    String class2 = "test.thread.Test2Test";
    
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("ParallelTestTest");
    xmlSuite.setParallel(XmlSuite.PARALLEL_TESTS);
    List<XmlTest> tests = xmlSuite.getTests();
    createTest(xmlSuite, class1);
    createTest(xmlSuite, class2);
    
    TestNG tng = new TestNG();
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { xmlSuite }));
    
    Helper.reset();
    
    tng.run();
    
    Map<Long, Long>[] maps = new Map[] {
        Helper.getMap(class1),
        Helper.getMap(class2),
    };

    for(Map m : maps) {
      Assert.assertEquals(m.size(), 1);
    }
    
    long[] ids = new long[] {
        maps[0].keySet().iterator().next().longValue(),
        maps[1].keySet().iterator().next().longValue(),
    };
    Map<Long, Long> verifyMap = new HashMap<Long, Long>();
    
    for (long id : ids) {
      verifyMap.put(id, id);
    }
    
    Assert.assertEquals(verifyMap.size(), 2);
    
    ppp("COUNT:" + threadCount  + " THREAD ID'S:" + ids[0] + " " + ids[1]);
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[SequentialTest] " + s);
    }
  }

}
