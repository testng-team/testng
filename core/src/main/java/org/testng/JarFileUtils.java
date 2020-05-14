package org.testng;

import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.reporters.Files;
import org.testng.util.Strings;
import org.testng.xml.IPostProcessor;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.TestNamesMatcher;
import org.testng.xml.internal.XmlSuiteUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** A Utility for extracting {@link XmlSuite} from a jar. */
class JarFileUtils {
  private final IPostProcessor processor;
  private final String xmlPathInJar;
  private final List<String> testNames;
  private final List<XmlSuite> suites = Lists.newLinkedList();
  private final XmlSuite.ParallelMode mode;

  JarFileUtils(IPostProcessor processor, String xmlPathInJar, List<String> testNames) {
    this(processor, xmlPathInJar, testNames, XmlSuite.ParallelMode.NONE);
  }

  JarFileUtils(
      IPostProcessor processor,
      String xmlPathInJar,
      List<String> testNames,
      XmlSuite.ParallelMode mode) {
    this.processor = processor;
    this.xmlPathInJar = xmlPathInJar;
    this.testNames = testNames;
    this.mode = mode == null ? XmlSuite.ParallelMode.NONE : mode;
  }

  List<XmlSuite> extractSuitesFrom(File jarFile) {
    try {

      Utils.log("TestNG", 2, "Trying to open jar file:" + jarFile);

      List<String> classes = Lists.newArrayList();
      boolean foundTestngXml = testngXmlExistsInJar(jarFile, classes);
      if (!foundTestngXml) {
        Utils.log(
            "TestNG",
            1,
            "Couldn't find the " + xmlPathInJar + " in the jar file, running all the classes");
        XmlSuite suite = XmlSuiteUtils.newXmlSuiteUsing(classes);
        suite.setParallel(this.mode);
        suites.add(suite);
      }
    } catch (IOException ex) {
      throw new TestNGException(ex);
    }
    return suites;
  }

  private boolean testngXmlExistsInJar(File jarFile, List<String> classes) throws IOException {
    try (JarFile jf = new JarFile(jarFile)) {
      Enumeration<JarEntry> entries = jf.entries();
      File file = java.nio.file.Files.createTempDirectory("testngXmlPathInJar-").toFile();
      String suitePath = null;
      while (entries.hasMoreElements()) {
        JarEntry je = entries.nextElement();
        String jeName = je.getName();
        if (Parser.canParse(jeName.toLowerCase())) {
          InputStream inputStream = jf.getInputStream(je);
          File copyFile = new File(file, jeName);
          Files.copyFile(inputStream, copyFile);
          if (matchesXmlPathInJar(je)) {
            suitePath = copyFile.toString();
          }
        } else if (isJavaClass(je)) {
          classes.add(constructClassName(je));
        }
      }
      if (Strings.isNullOrEmpty(suitePath)) {
        return false;
      }
      Collection<XmlSuite> parsedSuites = Parser.parse(suitePath, processor);
      delete(file);
      for (XmlSuite suite : parsedSuites) {
        // If test names were specified, only run these test names
        if (testNames != null) {
          TestNamesMatcher testNamesMatcher = new TestNamesMatcher(suite, testNames);
          List<String> missMatchedTestname = testNamesMatcher.getMissMatchedTestNames();
          if (!missMatchedTestname.isEmpty()) {
            throw new TestNGException("The test(s) <" + missMatchedTestname + "> cannot be found.");
          }
          suites.addAll(testNamesMatcher.getSuitesMatchingTestNames());
        } else {
          suites.add(suite);
        }
        return true;
      }
    }
    return false;
  }
  
  private void delete(File f) throws IOException {
    if (f.isDirectory()) {
      for (File c : f.listFiles())
        delete(c);
    }
    if (!f.delete())
      throw new FileNotFoundException("Failed to delete file: " + f);
  }

  private boolean matchesXmlPathInJar(JarEntry je) {
    return je.getName().equals(xmlPathInJar);
  }

  private static boolean isJavaClass(JarEntry je) {
    return je.getName().endsWith(".class");
  }

  private static String constructClassName(JarEntry je) {
    int n = je.getName().length() - ".class".length();
    return je.getName().replace("/", ".").substring(0, n);
  }
}
