package org.testng;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.util.Strings;
import org.testng.xml.IPostProcessor;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;
import org.testng.xml.internal.TestNamesMatcher;
import org.testng.xml.internal.XmlSuiteUtils;

/** A Utility for extracting {@link XmlSuite} from a jar. */
class JarFileUtils {
  private final IPostProcessor processor;
  private final String xmlPathInJar;
  private final boolean ignoreMissedTestNames;
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
    this(processor, xmlPathInJar, testNames, mode, false);
  }

  JarFileUtils(
      IPostProcessor processor,
      String xmlPathInJar,
      List<String> testNames,
      boolean ignoreMissedTestNames) {
    this(processor, xmlPathInJar, testNames, XmlSuite.ParallelMode.NONE, ignoreMissedTestNames);
  }

  JarFileUtils(
      IPostProcessor processor,
      String xmlPathInJar,
      List<String> testNames,
      XmlSuite.ParallelMode mode,
      boolean ignoreMissedTestNames) {
    this.processor = processor;
    this.xmlPathInJar = xmlPathInJar;
    this.testNames = testNames;
    this.mode = mode == null ? XmlSuite.ParallelMode.NONE : mode;
    this.ignoreMissedTestNames = ignoreMissedTestNames;
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
          if (!copyFile.toPath().normalize().startsWith(file.toPath().normalize())) {
            throw new IOException("Bad zip entry");
          }
          copyFile.getParentFile().mkdirs();
          Files.copy(inputStream, copyFile.toPath());
          if (matchesXmlPathInJar(je)) {
            suitePath = copyFile.toString();
          }
        } else if (isJavaClass(je)) {
          classes.add(constructClassName(je));
        }
      }

      if (Strings.isNullOrEmpty(suitePath)) {
        Utils.log("TestNG", 1, String.format("Not found '%s' in '%s'.", xmlPathInJar, jarFile));
        return false;
      }

      Collection<XmlSuite> parsedSuites = Parser.parse(suitePath, processor);
      delete(file);
      boolean addedSuite = false;
      for (XmlSuite suite : parsedSuites) {
        if (testNames == null) {
          suites.add(suite);
          addedSuite = true;
        } else {
          TestNamesMatcher testNamesMatcher =
              new TestNamesMatcher(suite, testNames, ignoreMissedTestNames);
          boolean validationResult = testNamesMatcher.validateMissMatchedTestNames();
          if (validationResult) {
            suites.addAll(testNamesMatcher.getSuitesMatchingTestNames());
            addedSuite = true;
          } else {
            Utils.error(String.format("None of '%s' found in '%s'.", testNames, suite));
          }
        }
      }

      return addedSuite;
    }
  }

  private void delete(File f) throws IOException {
    if (f.isDirectory()) {
      for (File c : Objects.requireNonNull(f.listFiles())) delete(c);
    }
    Files.deleteIfExists(f.toPath());
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
