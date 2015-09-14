package test.thread;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParallelTestTest extends BaseTest {

  @Test
  public void verifyParallelNone() {
    verifyExpected(XmlSuite.ParallelMode.NONE, 1);
  }

  @Test
  public void verifyParallelTests() {
    verifyExpected(XmlSuite.ParallelMode.TESTS, 2);
  }

  @Test
  public void verifyParallelMethods() {
    verifyExpected(XmlSuite.ParallelMode.METHODS, 4);
  }

  @Test
  public void verifyParallelClasses() {
    verifyExpected(XmlSuite.ParallelMode.CLASSES, 2);
  }

  @Test
  public void verifyParallelClassesWithFactory() {
    verifyExpected(XmlSuite.ParallelMode.INSTANCES, 2, ParallelWithFactorySampleTest.class.getName());
  }

  @Test
  public void verifyNonParallelClassesWithFactory() {
    verifyExpected(XmlSuite.ParallelMode.NONE, 1, ParallelWithFactorySampleTest.class.getName());
  }

  public static final String CLASS1 = "test.thread.Test1Test";
  public static final String CLASS2 = "test.thread.Test2Test";

  private void createTest(XmlSuite xmlSuite, String className) {
    XmlTest result = new XmlTest(xmlSuite);
    List<XmlClass> classes = result.getXmlClasses();
    XmlClass xmlClass = new XmlClass(className);
    classes.add(xmlClass);
  }

  private void verifyExpected(XmlSuite.ParallelMode parallelMode, int expectedThreadCount) {
    verifyExpected(parallelMode, expectedThreadCount, CLASS1, CLASS2);
  }

  private void verifyExpected(XmlSuite.ParallelMode parallelMode, int expectedThreadCount,
      String... classNames) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("ParallelTestTest");
    xmlSuite.setParallel(parallelMode);
    for (String cn : classNames) {
      createTest(xmlSuite, cn);
    }

    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { xmlSuite }));

    Helper.reset();

    tng.run();

    List<Map<Long, Long>> maps = Lists.newArrayList();
    for (String c : classNames) {
      maps.add(Helper.getMap(c));
    };

    Map<Long, Long> mergedMap = new HashMap<>();
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
