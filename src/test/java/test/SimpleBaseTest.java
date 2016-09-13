package test;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.collections.Lists;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SimpleBaseTest {
  // System property specifying where the resources (e.g. xml files) can be found
  private static final String TEST_RESOURCES_DIR = "test.resources.dir";

  public static TestNG create() {
    TestNG result = new TestNG();
    result.setUseDefaultListeners(false);
    result.setVerbose(0);
    return result;
  }

  public static TestNG create(Class<?>... testClasses) {
    TestNG result = create();
    result.setTestClasses(testClasses);
    return result;
  }

  protected static TestNG create(Path outputDir, Class<?>... testClasses) {
    TestNG result = create(testClasses);
    result.setOutputDirectory(outputDir.toAbsolutePath().toString());
    return result;
  }

  protected static TestNG create(XmlSuite... suites) {
    return create(Arrays.asList(suites));
  }

  protected static TestNG create(List<XmlSuite> suites) {
    TestNG result = create();
    result.setXmlSuites(suites);
    return result;
  }

  protected static TestNG create(Path outputDir, XmlSuite... suites) {
    return create(outputDir, Arrays.asList(suites));
  }

  protected static TestNG create(Path outputDir, List<XmlSuite> suites) {
    TestNG result = create(suites);
    result.setOutputDirectory(outputDir.toAbsolutePath().toString());
    return result;
  }

  protected static TestNG createTests(String suiteName, Class<?>... testClasses) {
    XmlSuite suite = createXmlSuite(suiteName);
    int i=0;
    for (Class<?> testClass : testClasses) {
      createXmlTest(suite, testClass.getName() + i, testClass);
      i++;
    }
    return create(suite);
  }

  protected static XmlSuite createXmlSuite(String name) {
    XmlSuite result = new XmlSuite();
    result.setName(name);
    return result;
  }

  protected static XmlSuite createXmlSuite(String suiteName, String testName, Class<?>... classes) {
    XmlSuite suite = createXmlSuite(suiteName);
    createXmlTest(suite, testName, classes);
    return suite;
  }

  protected static XmlTest createXmlTest(XmlSuite suite, String name) {
    XmlTest result = new XmlTest(suite);
    result.setName(name);
    return result;
  }

  protected static XmlTest createXmlTest(XmlSuite suite, String name, Class<?>... classes) {
    XmlTest result = createXmlTest(suite, name);
    int index = 0;
    for (Class<?> c : classes) {
      XmlClass xc = new XmlClass(c.getName(), index++, true /* load classes */);
      result.getXmlClasses().add(xc);
    }

    return result;
  }

  protected static XmlClass createXmlClass(XmlTest test, Class<?> testClass) {
    XmlClass clazz = new XmlClass(testClass);
    test.getXmlClasses().add(clazz);
    return clazz;
  }

  protected static XmlInclude createXmlInclude(XmlClass clazz, String method) {
    XmlInclude include = new XmlInclude(method);

    include.setXmlClass(clazz);
    clazz.getIncludedMethods().add(include);

    return include;
  }

  protected static XmlTest createXmlTest(XmlSuite suite, String name, String... classes) {
    XmlTest result = createXmlTest(suite, name);
    int index = 0;
    for (String c : classes) {
      XmlClass xc = new XmlClass(c, index++, true /* load classes */);
      result.getXmlClasses().add(xc);
    }

    return result;
  }

  protected static void addMethods(XmlClass cls, String... methods) {
    int index = 0;
    for (String method : methods) {
      XmlInclude include = new XmlInclude(method, index++);
      cls.getIncludedMethods().add(include);
    }
  }

  public static String getPathToResource(String fileName) {
    String result = System.getProperty(TEST_RESOURCES_DIR);
    if (result == null) {
      throw new IllegalArgumentException("System property " + TEST_RESOURCES_DIR + " was not defined.");
    }
    return result + File.separatorChar + fileName;
  }

  protected static void verifyPassedTests(TestListenerAdapter tla, String... methodNames) {
    Iterator<ITestResult> it = tla.getPassedTests().iterator();
    Assert.assertEquals(tla.getPassedTests().size(), methodNames.length);

    int i = 0;
    while (it.hasNext()) {
      Assert.assertEquals(it.next().getName(), methodNames[i++]);
    }
  }

  /**
   * Compare a list of ITestResult with a list of String method names,
   */
  public static void assertTestResultsEqual(List<ITestResult> results, List<String> methods) {
    List<String> resultMethods = Lists.newArrayList();
    for (ITestResult r : results) {
      resultMethods.add(r.getMethod().getMethodName());
    }
    Assert.assertEquals(resultMethods, methods);
  }

}
