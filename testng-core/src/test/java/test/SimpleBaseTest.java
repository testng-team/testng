package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlRun;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;

public class SimpleBaseTest {
  // System property specifying where the resources (e.g. xml files) can be found
  private static final String TEST_RESOURCES_DIR = "test.resources.dir";

  public static InvokedMethodNameListener run(Class<?>... testClasses) {
    return run(false, testClasses);
  }

  public static InvokedMethodNameListener run(boolean skipConfiguration, Class<?>... testClasses) {
    TestNG tng = create(testClasses);

    return run(skipConfiguration, tng);
  }

  public static InvokedMethodNameListener run(XmlSuite... suites) {
    return run(false, suites);
  }

  public static InvokedMethodNameListener run(boolean skipConfiguration, XmlSuite... suites) {
    TestNG tng = create(suites);

    return run(skipConfiguration, tng);
  }

  private static InvokedMethodNameListener run(boolean skipConfiguration, TestNG tng) {
    InvokedMethodNameListener listener = new InvokedMethodNameListener(skipConfiguration);
    tng.addListener(listener);

    tng.run();

    return listener;
  }

  public static TestNG create() {
    TestNG result = new TestNG();
    result.setUseDefaultListeners(false);
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
    return createTests(null, suiteName, testClasses);
  }

  protected static TestNG createTests(Path outDir, String suiteName, Class<?>... testClasses) {
    XmlSuite suite = createXmlSuite(suiteName);
    int i = 0;
    for (Class<?> testClass : testClasses) {
      createXmlTest(suite, testClass.getName() + i, testClass);
      i++;
    }
    if (outDir == null) {
      return create(suite);
    }
    return create(outDir, suite);
  }

  protected static XmlSuite createDummySuiteWithTestNamesAs(String... tests) {
    XmlSuite suite = new XmlSuite();
    suite.setName("random_suite");
    for (String test : tests) {
      XmlTest xmlTest = new XmlTest(suite);
      xmlTest.setName(test);
    }
    return suite;
  }

  protected static XmlSuite createXmlSuite(String name) {
    XmlSuite result = new XmlSuite();
    result.setName(name);
    return result;
  }

  protected static XmlSuite createXmlSuite(Map<String, String> params) {
    XmlSuite result = createXmlSuite(UUID.randomUUID().toString());
    result.setParameters(params);
    return result;
  }

  protected static XmlSuite createXmlSuite(String suiteName, String testName, Class<?>... classes) {
    XmlSuite suite = createXmlSuite(suiteName);
    createXmlTest(suite, testName, classes);
    return suite;
  }

  protected static XmlSuite createXmlSuite(String suiteName, Map<String, String> params) {
    XmlSuite suite = createXmlSuite(suiteName);
    suite.setParameters(params);
    return suite;
  }

  protected static XmlTest createXmlTestWithPackages(
      XmlSuite suite, String name, String... packageName) {
    XmlTest result = createXmlTest(suite, name);
    List<XmlPackage> xmlPackages = Lists.newArrayList();

    for (String each : packageName) {
      XmlPackage xmlPackage = new XmlPackage();
      xmlPackage.setName(each);
      xmlPackages.add(xmlPackage);
    }
    result.setPackages(xmlPackages);

    return result;
  }

  protected static XmlTest createXmlTestWithPackages(
      XmlSuite suite, String name, Class<?>... packageName) {
    XmlTest result = createXmlTest(suite, name);
    List<XmlPackage> xmlPackages = Lists.newArrayList();

    for (Class<?> each : packageName) {
      XmlPackage xmlPackage = new XmlPackage();
      xmlPackage.setName(each.getPackage().getName());
      xmlPackages.add(xmlPackage);
    }
    result.setPackages(xmlPackages);

    return result;
  }

  protected static XmlTest createXmlTest(String suiteName, String testName) {
    XmlSuite suite = createXmlSuite(suiteName);
    return createXmlTest(suite, testName);
  }

  protected static XmlTest createXmlTest(String suiteName, String testName, Class<?>... classes) {
    XmlSuite suite = createXmlSuite(suiteName);
    XmlTest xmlTest = createXmlTest(suite, testName);
    for (Class<?> clazz : classes) {
      xmlTest.getXmlClasses().add(new XmlClass(clazz));
    }
    return xmlTest;
  }

  protected static XmlTest createXmlTest(XmlSuite suite, String name) {
    XmlTest result = new XmlTest(suite);
    result.setName(name);
    return result;
  }

  protected static XmlTest createXmlTest(XmlSuite suite, String name, Map<String, String> params) {
    XmlTest result = new XmlTest(suite);
    result.setName(name);
    result.setParameters(params);
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

  protected static XmlClass createXmlClass(
      XmlTest test, Class<?> testClass, Map<String, String> params) {
    XmlClass clazz = createXmlClass(test, testClass);
    clazz.setParameters(params);
    return clazz;
  }

  protected static XmlInclude createXmlInclude(XmlClass clazz, String method) {
    XmlInclude include = new XmlInclude(method);

    include.setXmlClass(clazz);
    clazz.getIncludedMethods().add(include);

    return include;
  }

  protected static XmlInclude createXmlInclude(
      XmlClass clazz, String method, Map<String, String> params) {
    XmlInclude include = createXmlInclude(clazz, method);
    include.setParameters(params);
    return include;
  }

  protected static XmlInclude createXmlInclude(
      XmlClass clazz, String method, int index, Integer... list) {
    XmlInclude include = new XmlInclude(method, Arrays.asList(list), index);

    include.setXmlClass(clazz);
    clazz.getIncludedMethods().add(include);

    return include;
  }

  protected static XmlGroups createXmlGroups(XmlSuite suite, String... includedGroupNames) {
    XmlGroups xmlGroups = createGroupIncluding(includedGroupNames);
    suite.setGroups(xmlGroups);
    return xmlGroups;
  }

  protected static XmlGroups createXmlGroups(XmlTest test, String... includedGroupNames) {
    XmlGroups xmlGroups = createGroupIncluding(includedGroupNames);
    test.setGroups(xmlGroups);
    return xmlGroups;
  }

  private static XmlGroups createGroupIncluding(String... groupNames) {
    XmlGroups xmlGroups = new XmlGroups();
    XmlRun xmlRun = new XmlRun();
    for (String group : groupNames) {
      xmlRun.onInclude(group);
    }
    xmlGroups.setRun(xmlRun);
    return xmlGroups;
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
    String result = System.getProperty(TEST_RESOURCES_DIR, "src/test/resources");
    if (result == null) {
      throw new IllegalArgumentException(
          "System property " + TEST_RESOURCES_DIR + " was not defined.");
    }
    return result + File.separatorChar + fileName;
  }

  public static List<ITestNGMethod> extractTestNGMethods(Class<?>... classes) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "tests", classes);
    IAnnotationFinder annotationFinder =
        new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
    List<ITestNGMethod> methods = Lists.newArrayList();
    for (Class<?> clazz : classes) {
      methods.addAll(
          Arrays.asList(
              AnnotationHelper.findMethodsWithAnnotation(
                  new ITestObjectFactory() {},
                  clazz,
                  ITestAnnotation.class,
                  annotationFinder,
                  xmlTest)));
    }
    return methods;
  }

  /** Compare a list of ITestResult with a list of String method names, */
  protected static void assertTestResultsEqual(List<ITestResult> results, List<String> methods) {
    List<String> resultMethods = Lists.newArrayList();
    for (ITestResult r : results) {
      resultMethods.add(r.getMethod().getMethodName());
    }
    Assert.assertEquals(resultMethods, methods);
  }

  /** Deletes all files and subdirectories under dir. */
  protected static void deleteDir(File dir) {
    try {
      Files.walkFileTree(
          dir.toPath(),
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              Files.delete(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected static File createDirInTempDir(String dir) {
    File slashTmpDir = new File(System.getProperty("java.io.tmpdir"));
    File mTempDirectory = new File(slashTmpDir, dir);
    mTempDirectory.mkdirs();
    mTempDirectory.deleteOnExit();
    return mTempDirectory;
  }

  /**
   * @param fileName The filename to parse
   * @param regexp The regular expression
   * @param resultLines An out parameter that will contain all the lines that matched the regexp
   * @return A List<Integer> containing the lines of all the matches
   *     <p>Note that the size() of the returned valuewill always be equal to result.size() at the
   *     end of this function.
   */
  protected static List<Integer> grep(File fileName, String regexp, List<String> resultLines) {
    List<Integer> resultLineNumbers = new ArrayList<>();
    try (Reader reader = new FileReader(fileName)) {
      resultLineNumbers = grep(reader, regexp, resultLines);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultLineNumbers;
  }

  protected static List<Integer> grep(Reader reader, String regexp, List<String> resultLines) {
    List<Integer> resultLineNumbers = new ArrayList<>();
    try (BufferedReader fr = new BufferedReader(reader)) {
      String line;
      int currentLine = 0;
      Pattern p = Pattern.compile(".*" + regexp + ".*");
      while ((line = fr.readLine()) != null) {
        if (p.matcher(line).matches()) {
          resultLines.add(line);
          resultLineNumbers.add(currentLine);
        }
        currentLine++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultLineNumbers;
  }

  protected static List<XmlSuite> getSuites(String... suiteFiles) {
    List<XmlSuite> suites = new ArrayList<>();
    for (String suiteFile : suiteFiles) {
      try {
        suites.addAll(new Parser(suiteFile).parseToList());
      } catch (IOException e) {
        throw new TestNGException(e);
      }
    }
    return suites;
  }

  protected static String getFailedResultMessage(List<ITestResult> testResultList) {
    String methods =
        testResultList.stream()
            .map(
                r ->
                    new AbstractMap.SimpleEntry<>(
                        r.getMethod().getQualifiedName(), r.getThrowable()))
            .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n"));
    return String.format("Failed methods should pass:\n %s\n", methods);
  }
}
