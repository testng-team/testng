package test.thread;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
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

  @DataProvider
  private static Object[][] verifyParallelDp() {
    return new Object[][]{
            // isCommandLine, parallelMode, expectedThreadCount
            {true,  XmlSuite.ParallelMode.NONE,      1},
            {false, XmlSuite.ParallelMode.NONE,      1},
            {true,  XmlSuite.ParallelMode.TESTS,     2},
            {false, XmlSuite.ParallelMode.TESTS,     2},
            {true,  XmlSuite.ParallelMode.METHODS,   4},
            {false, XmlSuite.ParallelMode.METHODS,   4},
            {true,  XmlSuite.ParallelMode.CLASSES,   2},
            {false, XmlSuite.ParallelMode.CLASSES,   2},
            {true,  XmlSuite.ParallelMode.INSTANCES, 2},
            {false, XmlSuite.ParallelMode.INSTANCES, 2}
    };
  }

  @Test(dataProvider = "verifyParallelDp")
  public void verifyParallel(boolean isCommandLine, XmlSuite.ParallelMode parallelMode, int expectedThreadCount) {
    verifyExpected(isCommandLine, parallelMode, expectedThreadCount, Test1Test.class, Test2Test.class);
  }

  @DataProvider
  private static Object[][] verifyParallelWithFactoryDp() {
    return new Object[][]{
            // isCommandLine, parallelMode, expectedThreadCount
            {true,  XmlSuite.ParallelMode.NONE,      1},
            {false, XmlSuite.ParallelMode.NONE,      1},
            {true,  XmlSuite.ParallelMode.INSTANCES, 2},
            {false, XmlSuite.ParallelMode.INSTANCES, 2}
    };
  }

  @Test(dataProvider = "verifyParallelWithFactoryDp") // TODO use "verifyParallelDp"
  public void verifyParallelWithFactory(boolean isCommandLine, XmlSuite.ParallelMode parallelMode, int expectedThreadCount) {
    verifyExpected(isCommandLine, parallelMode, expectedThreadCount, ParallelWithFactorySampleTest.class);
  }

  private void createTest(XmlSuite xmlSuite, Class<?> clazz) {
    XmlTest result = new XmlTest(xmlSuite);
    List<XmlClass> classes = result.getXmlClasses();
    XmlClass xmlClass = new XmlClass(clazz);
    classes.add(xmlClass);
  }

  private void verifyExpected(boolean isCommandLine, XmlSuite.ParallelMode parallelMode, int expectedThreadCount,
      Class<?>... classes) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("ParallelTestTest");
    xmlSuite.setParallel(parallelMode);
    for (Class<?> clazz : classes) {
      createTest(xmlSuite, clazz);
    }

    TestNG tng = new TestNG();
    tng.setVerbose(0);
    if (isCommandLine) {
      tng.setCommandLineSuite(xmlSuite);
    } else {
      tng.setXmlSuites(Arrays.asList(xmlSuite));
    }

    Helper.reset();

    tng.run();

    List<Map<Long, Long>> maps = Lists.newArrayList();
    for (Class<?> clazz : classes) {
      maps.add(Helper.getMap(clazz.getName()));
    }

    Map<Long, Long> mergedMap = new HashMap<>();
    for (Map<Long, Long> m : maps) {
      mergedMap.putAll(m);
    }

    Assert.assertEquals(mergedMap.size(), expectedThreadCount);
  }
}
