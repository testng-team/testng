package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.testng.SkipException;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.BaseTest;

public class ParallelTestTest extends BaseTest {

  @DataProvider
  private static Object[][] verifyParallelDp() {
    return new Object[][] {
      // isCommandLine, parallelMode, expectedThreadCount
      {true, XmlSuite.ParallelMode.NONE, 1},
      {false, XmlSuite.ParallelMode.NONE, 1},
      {true, XmlSuite.ParallelMode.TESTS, 2},
      {false, XmlSuite.ParallelMode.TESTS, 2},
      {true, XmlSuite.ParallelMode.METHODS, 4},
      {false, XmlSuite.ParallelMode.METHODS, 4},
      {true, XmlSuite.ParallelMode.CLASSES, 2},
      {false, XmlSuite.ParallelMode.CLASSES, 2},
      {true, XmlSuite.ParallelMode.INSTANCES, 4},
      {false, XmlSuite.ParallelMode.INSTANCES, 4}
    };
  }

  @Test(dataProvider = "verifyParallelDp")
  public void verifyParallel(
      boolean isCommandLine, XmlSuite.ParallelMode parallelMode, int expectedThreadCount) {
    verifyExpected(
        isCommandLine, parallelMode, expectedThreadCount, Test1Test.class, Test2Test.class);
  }

  @DataProvider
  private static Object[][] verifyParallelWithFactoryDp() {
    return new Object[][] {
      // isCommandLine, parallelMode, expectedThreadCount
      {true, XmlSuite.ParallelMode.NONE, 1},
      {false, XmlSuite.ParallelMode.NONE, 1},
      {true, XmlSuite.ParallelMode.INSTANCES, 4},
      {false, XmlSuite.ParallelMode.INSTANCES, 4}
    };
  }

  @Test(dataProvider = "verifyParallelWithFactoryDp") // TODO use "verifyParallelDp"
  public void verifyParallelWithFactory(
      boolean isCommandLine, XmlSuite.ParallelMode parallelMode, int expectedThreadCount) {
    verifyExpected(
        isCommandLine, parallelMode, expectedThreadCount, ParallelWithFactorySampleTest.class);
  }

  @Test
  public void testParallelModeInheritanceInCommandlineMode() throws FileNotFoundException {
    SuiteXmlParser parser = new SuiteXmlParser();
    String file = "src/test/resources/1636.xml";
    XmlSuite xmlSuite = parser.parse(file, new FileInputStream(file), true);
    TestNG tng = new TestNG();
    tng.setXmlSuites(Collections.singletonList(xmlSuite));
    tng.run();
    assertThat(Github1636Sample.threads).hasSize(3);
  }

  @Test
  public void testJarXmlSuiteObeyCommandLine() {
    final String file = "build/resources/test/2532.zip";
    final String suiteFileName = "2532.xml";
    final int threadCount = 3;
    zipResource(suiteFileName, file);

    TestNG tng = new TestNG();
    tng.setXmlPathInJar(suiteFileName);
    tng.setTestJar(file);
    tng.setThreadCount(threadCount);
    tng.setParallel(XmlSuite.ParallelMode.METHODS);
    tng.run();
    assertThat(Github1636Sample.threads).hasSize(threadCount);
  }

  @Test
  public void testXmlSuiteObeyCommandLine() {
    final String file = "build/resources/test/2532.xml";
    final int threadCount = 3;

    TestNG tng = new TestNG();
    tng.setTestSuites(Collections.singletonList(file));
    tng.setThreadCount(threadCount);
    tng.setParallel(XmlSuite.ParallelMode.METHODS);
    tng.run();
    assertThat(Github1636Sample.threads).hasSize(threadCount);
  }

  private void createTest(XmlSuite xmlSuite, Class<?> clazz) {
    XmlTest result = new XmlTest(xmlSuite);
    List<XmlClass> classes = result.getXmlClasses();
    XmlClass xmlClass = new XmlClass(clazz);
    classes.add(xmlClass);
  }

  private void verifyExpected(
      boolean isCommandLine,
      XmlSuite.ParallelMode parallelMode,
      int expectedThreadCount,
      Class<?>... classes) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("ParallelTestTest");
    xmlSuite.setParallel(parallelMode);
    for (Class<?> clazz : classes) {
      createTest(xmlSuite, clazz);
    }

    TestNG tng = new TestNG();
    if (isCommandLine) {
      tng.setCommandLineSuite(xmlSuite);
    } else {
      tng.setXmlSuites(Collections.singletonList(xmlSuite));
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

    assertThat(mergedMap).hasSize(expectedThreadCount);
  }

  private void zipResource(String resourceName, String targetFilePath) {
    InputStream suiteStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
    try {
      File targetFile = new File(targetFilePath);
      FileOutputStream targetFileStream = new FileOutputStream(targetFile);
      ZipOutputStream zipSuiteStream = new ZipOutputStream(targetFileStream);
      zipSuiteStream.putNextEntry(new ZipEntry(resourceName));

      byte[] suiteByteData = IOUtils.toByteArray(suiteStream);
      zipSuiteStream.write(suiteByteData, 0, suiteByteData.length);
      zipSuiteStream.closeEntry();
      zipSuiteStream.close();
    } catch (IOException e) {
      throw new SkipException(String.format("Error writing zip to %s", targetFilePath), e);
    }
  }

  @AfterMethod
  private void clearThreadCount() {
    Github1636Sample.threads.clear();
  }
}
