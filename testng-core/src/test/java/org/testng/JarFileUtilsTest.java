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
import org.testng.jarfileutils.JarCreator;
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
          "\nThe test\\(s\\) <\\[testng-tests-child11\\]> cannot be found.")
  public void testWithInvalidTestNames() throws MalformedURLException {
    JarFileUtils utils = newJarFileUtils(Collections.singletonList("testng-tests-child11"));
    runTest(
        utils,
        1,
        new String[] {"testng-tests-child1"},
        new String[] {"org.testng.jarfileutils.org.testng.SampleTest1"});
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
    for (XmlSuite xmlSuite : suites) {
      extractClassNames(xmlSuite, testNames, classNames);
    }

    assertThat(testNames).containsExactly(expectedTestNames);
    assertThat(classNames).contains(expectedClassNames);
  }

  private static void extractClassNames(
      XmlSuite xmlSuite, List<String> testNames, List<String> classNames) {
    for (XmlTest xmlTest : xmlSuite.getTests()) {
      testNames.add(xmlTest.getName());
      for (XmlClass xmlClass : xmlTest.getXmlClasses()) {
        classNames.add(xmlClass.getName());
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
    List<XmlSuite> suites = utils.extractSuitesFrom(jar);
    assertThat(suites).hasSize(1);
    XmlSuite suite = suites.get(0);
    assertThat(suite.getName()).isEqualTo(expectedSuiteName);
    assertThat(suite.getTests()).hasSize(numberOfTests);
    List<String> testNames = new LinkedList<>();
    List<String> classNames = new LinkedList<>();
    extractClassNames(suite, testNames, classNames);
    if (expectedTestNames != null) {
      assertThat(testNames).containsExactly(expectedTestNames);
    }
    assertThat(classNames).contains(expectedClassNames);
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
