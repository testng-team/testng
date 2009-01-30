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
    
  @Test
  public void verifyParallelNone() {
    verifyExpected(XmlSuite.PARALLEL_NONE, 1);
  }
  
  @Test
  public void verifyParallelTests() {
    verifyExpected(XmlSuite.PARALLEL_TESTS, 2);
  }

  @Test
  public void verifyParallelMethods() {
    verifyExpected(XmlSuite.PARALLEL_METHODS, 4);
  }
  
  @Test
  public void verifyParallelClasses() {
    verifyExpected(XmlSuite.PARALLEL_CLASSES, 2);
  }

  public static final String CLASS1 = "test.thread.Test1Test";
  public static final String CLASS2 = "test.thread.Test2Test";
    
  private void createTest(XmlSuite xmlSuite, String className) {
    XmlTest result = new XmlTest(xmlSuite);
    List<XmlClass> classes = result.getXmlClasses();
    XmlClass xmlClass = new XmlClass(className);
    classes.add(xmlClass);
  }
  
  private void verifyExpected(String parallelMode, int expectedThreadCount) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("ParallelTestTest");
    xmlSuite.setParallel(parallelMode);
    createTest(xmlSuite, CLASS1);
    createTest(xmlSuite, CLASS2);
    
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { xmlSuite }));
    
    Helper.reset();
    
    tng.run();    
    
    Map<Long, Long>[] maps = new Map[] {
        Helper.getMap(CLASS1),
        Helper.getMap(CLASS2),
    };
    
    Map<Long, Long> mergedMap = new HashMap<Long, Long>();
    for (Map<Long, Long>m : maps) {
      mergedMap.putAll(m);
    }
    
    Assert.assertEquals(mergedMap.size(), expectedThreadCount);
  }

  private static void ppp(String s) {
    if (false) {
      System.out.println("[SequentialTest] " + s);
    }
  }

}
