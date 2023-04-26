package org.testng;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;
import org.testng.testhelper.JarCreator;
import org.testng.xml.IPostProcessor;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class JarFileUtilsTest {
  private static File jar = null;

  @BeforeClass
  public void generateTestJar() throws IOException {
    jar = JarCreator.generateJar();
  }

  @Test
  public void testWithValidTestNames() throws MalformedURLException {
    JarFileUtils utils = newJarFileUtils(Collections.singletonList("testng-tests-child1"));
    runTest(
        utils,
        1,
        new String[] {"testng-tests-child1"},
        new String[] {"org.testng.jarfileutils.org.testng.SampleTest1"});
  }

  @Test
  public void testWithNoTestNames() throws MalformedURLException {
    JarFileUtils utils = newJarFileUtils(null);
    runTest(
        utils,
        3,
        new String[] {"testng-tests-child1", "testng-tests-child2", "testng-tests-child3"},
        new String[] {
          "org.testng.jarfileutils.org.testng.SampleTest1",
          "org.testng.jarfileutils.org.testng.SampleTest2",
          "org.testng.jarfileutils.org.testng.SampleTest3"
        });
  }

  @Test(
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp =
          "\nThe test\\(s\\) <\\[testng-tests-child11\\]> cannot be found in suite.")
  public void testWithInvalidTestNames() throws MalformedURLException {
    JarFileUtils utils = newJarFileUtils(Collections.singletonList("testng-tests-child11"));
    runTest(
        utils,
        1,
        new String[] {"testng-tests-child1"},
        new String[] {"org.testng.jarfileutils.org.testng.SampleTest1"});
  }

  @Test(
      description =
          "GITHUB-2897, No TestNGException thrown when ignoreMissedTestNames enabled by System property 'testng.ignore.missed.testnames' and no test to run when all given test names are invalid.")
  public void testWithAllInvalidTestNamesNoExceptionIfIgnoreMissedTestNamesEnabledBySystemProperty()
      throws MalformedURLException {
    String oldIgnoreMissedTestNames =
        System.getProperty(RuntimeBehavior.TESTNG_IGNORE_MISSED_TESTNAMES, "false");
    try {
      System.setProperty(RuntimeBehavior.TESTNG_IGNORE_MISSED_TESTNAMES, "true");
      JarFileUtils utils = newJarFileUtils(Collections.singletonList("testng-tests-child11"));
      runTest(utils, 1, null, null, "Jar suite");
    } finally {
      System.setProperty(RuntimeBehavior.TESTNG_IGNORE_MISSED_TESTNAMES, oldIgnoreMissedTestNames);
    }
  }

  @Test(
      description =
          "GITHUB-2897, No TestNGException thrown when ignoreMissedTestNames enabled by System property 'testng.ignore.missed.testnames' and partial valid test names are expected to run.")
  public void
      testWithPartialInvalidTestNamesNoExceptionIfIgnoreMissedTestNamesEnabledBySystemProperty()
          throws MalformedURLException {
    String oldIgnoreMissedTestNames =
        System.getProperty(RuntimeBehavior.TESTNG_IGNORE_MISSED_TESTNAMES, "false");
    try {
      System.setProperty(RuntimeBehavior.TESTNG_IGNORE_MISSED_TESTNAMES, "true");
      String[] expectedTestNames =
          new String[] {"testng-tests-child2", "testng-tests-child4", "testng-tests-child5"};
      String[] expectedClassNames =
          new String[] {
            "org.testng.jarfileutils.org.testng.SampleTest2",
            "org.testng.jarfileutils.org.testng.SampleTest4",
            "org.testng.jarfileutils.org.testng.SampleTest5"
          };
      List<String> testNames =
          Arrays.asList(
              "testng-tests-child2", "testng-tests-child4", "testng-tests-child5", "invalid");
      JarFileUtils utils = newJarFileUtils(testNames);
      // 3 tests from 3 suites, the first suite has one test is given
      runTest(utils, 1, 3, expectedTestNames, expectedClassNames, "testng-tests-suite");
    } finally {
      System.setProperty(RuntimeBehavior.TESTNG_IGNORE_MISSED_TESTNAMES, oldIgnoreMissedTestNames);
    }
  }

  @Test
  public void testWithInvalidXmlFile() throws MalformedURLException {
    JarFileUtils utils =
        newJarFileUtils(
            "invalid-testng-tests.xml", Collections.singletonList("testng-tests-child11"));
    runTest(
        utils,
        1,
        null,
        new String[] {
          "org.testng.jarfileutils.org.testng.SampleTest1",
          "org.testng.jarfileutils.org.testng.SampleTest2",
          "org.testng.jarfileutils.org.testng.SampleTest3"
        },
        "Jar suite");
  }

  /**
   * Test to ensure that exception is not thrown. Ensure that GITHUB-2709 can not happen again.
   *
   * @throws MalformedURLException
   */
  @Test
  public void ensureThatExceptionAreNotThrown() throws MalformedURLException {
    TestNG testNg = new TestNG(false);
    List<String> testNames =
        Arrays.asList("testng-tests-child2", "testng-tests-child4", "testng-tests-child5");
    testNg.setTestNames(testNames);
    testNg.setXmlPathInJar("jarfileutils/testng-tests.xml");
    testNg.setTestJar(jar.getAbsolutePath());
    testNg.initializeSuitesAndJarFile();
    // "testng-tests-child2", "testng-tests-child4", "testng-tests-child5" are from 3 different test
    // suites
    Assert.assertEquals(testNg.m_suites.size(), 3);
  }

  /**
   * Test to ensure that exception is thrown for invalid test name.
   *
   * @throws MalformedURLException
   */
  @Test(
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp = "\nThe test\\(s\\) <\\[dummy\\]> cannot be found in suite.")
  public void ensureThatExceptionAreThrown() throws MalformedURLException {
    TestNG testNg = new TestNG(false);
    List<String> testNames =
        Arrays.asList("testng-tests-child2", "testng-tests-child4", "testng-tests-child5", "dummy");
    testNg.setTestNames(testNames);
    testNg.setXmlPathInJar("jarfileutils/testng-tests.xml");
    testNg.setTestJar(jar.getAbsolutePath());
    testNg.initializeSuitesAndJarFile();
    Assert.assertEquals(testNg.m_suites.size(), 1);
  }

  @Test
  public void testWithValidTestNamesFromMultiChildSuites() throws MalformedURLException {
    JarFileUtils utils =
        newJarFileUtils(
            Arrays.asList("testng-tests-child2", "testng-tests-child4", "testng-tests-child5"));
    String[] expectedTestNames =
        new String[] {"testng-tests-child2", "testng-tests-child4", "testng-tests-child5"};
    String[] expectedClassNames =
        new String[] {
          "org.testng.jarfileutils.org.testng.SampleTest2",
          "org.testng.jarfileutils.org.testng.SampleTest4",
          "org.testng.jarfileutils.org.testng.SampleTest5"
        };
    List<XmlSuite> suites = utils.extractSuitesFrom(jar);
    assertThat(suites).hasSize(3);
    XmlSuite suite = suites.get(0);
    assertThat(suite.getName()).isEqualTo("testng-tests-suite");
    List<String> testNames = new LinkedList<>();
    List<String> classNames = new LinkedList<>();
    extractClassNames(suites, testNames, classNames);

    assertThat(testNames).containsExactly(expectedTestNames);
    assertThat(classNames).contains(expectedClassNames);
  }

  private static void extractClassNames(
      List<XmlSuite> xmlSuites, List<String> testNames, List<String> classNames) {
    for (XmlSuite xmlSuite : xmlSuites) {
      for (XmlTest xmlTest : xmlSuite.getTests()) {
        testNames.add(xmlTest.getName());
        for (XmlClass xmlClass : xmlTest.getXmlClasses()) {
          classNames.add(xmlClass.getName());
        }
      }
    }
  }

  private static void runTest(
      JarFileUtils utils,
      int numberOfTests,
      String[] expectedTestNames,
      String[] expectedClassNames) {
    runTest(utils, numberOfTests, expectedTestNames, expectedClassNames, "testng-tests-suite");
  }

  private static void runTest(
      JarFileUtils utils,
      int numberOfTests,
      String[] expectedTestNames,
      String[] expectedClassNames,
      String expectedSuiteName) {
    runTest(utils, numberOfTests, 1, expectedTestNames, expectedClassNames, expectedSuiteName);
  }

  private static void runTest(
      JarFileUtils utils,
      int numberOfTests,
      int expectedSuiteTotal,
      String[] expectedTestNames,
      String[] expectedClassNames,
      String expectedSuiteName) {
    List<XmlSuite> suites = utils.extractSuitesFrom(jar);
    assertThat(suites).hasSize(expectedSuiteTotal);
    XmlSuite suite = suites.get(0);
    assertThat(suite.getName()).isEqualTo(expectedSuiteName);
    assertThat(suite.getTests()).hasSize(numberOfTests);
    List<String> testNames = new LinkedList<>();
    List<String> classNames = new LinkedList<>();
    extractClassNames(suites, testNames, classNames);
    if (expectedTestNames != null) {
      assertThat(testNames).containsExactly(expectedTestNames);
    }
    if (expectedClassNames != null) {
      assertThat(classNames).contains(expectedClassNames);
    }
  }

  public static class FakeProcessor implements IPostProcessor {

    @Override
    public Collection<XmlSuite> process(Collection<XmlSuite> suites) {
      return suites;
    }
  }

  private static JarFileUtils newJarFileUtils(List<String> testNames) throws MalformedURLException {
    return newJarFileUtils("jarfileutils/testng-tests.xml", testNames);
  }

  private static JarFileUtils newJarFileUtils(String suiteXmlName, List<String> testNames)
      throws MalformedURLException {
    URL url = jar.toURI().toURL();
    URLClassLoader classLoader =
        new URLClassLoader(new URL[] {url}, ClassLoader.getSystemClassLoader());
    Thread.currentThread().setContextClassLoader(classLoader);
    return new JarFileUtils(new FakeProcessor(), suiteXmlName, testNames);
  }
}
