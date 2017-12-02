package org.testng;

import org.testng.collections.CollectionUtils;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.reporters.Files;
import org.testng.util.Strings;
import org.testng.xml.IPostProcessor;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.XmlSuiteUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A Utility for extracting {@link XmlSuite} from a jar.
 */
class JarFileUtils {
    private final IPostProcessor processor;
    private final String xmlPathInJar;
    private final List<String> testNames;
    private final List<XmlSuite> suites = Lists.newLinkedList();

    JarFileUtils(IPostProcessor processor, String xmlPathInJar, List<String> testNames) {
        this.processor = processor;
        this.xmlPathInJar = xmlPathInJar;
        //Ensure that dynamic list operations supported
        this.testNames = (testNames == null ? null : new ArrayList<>(testNames));
    }

    List<XmlSuite> extractSuitesFrom(File jarFile) {
        try {

            Utils.log("TestNG", 2, "Trying to open jar file:" + jarFile);

            List<String> classes = Lists.newArrayList();
            boolean foundTestngXml = testngXmlExistsInJar(jarFile, classes);
            if (!foundTestngXml) {
                Utils.log("TestNG", 1,
                        "Couldn't find the " + xmlPathInJar + " in the jar file, running all the classes");
                suites.add(XmlSuiteUtils.newXmlSuiteUsing(classes));
            }
        } catch (IOException ex) {
            throw new TestNGException(ex);
        }
        return suites;
    }

    private boolean testngXmlExistsInJar(File jarFile, List<String> classes) throws IOException {
        boolean foundTestngXml = false;
        try (JarFile jf = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jf.entries();
            File file = createTempDir();
            String suitePath = null;
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                if (je.getName().toLowerCase().endsWith(".xml")){
                    String jeName = je.getName();
                    InputStream  inputStream = jf.getInputStream(je);
                    File copyFile = new File(file, jeName);
                    Files.copyFile(inputStream, copyFile);
                    if (matchesXmlPathInJar(je)) {
                        suitePath = copyFile.toString();
                    }
                }else if (isJavaClass(je)) {
                    classes.add(constructClassName(je));
                }
            }
            if(Strings.isNullOrEmpty(suitePath)){
                return foundTestngXml;
            }
            Collection<XmlSuite> parsedSuites = Parser.parse(suitePath,processor ); 
            for (XmlSuite suite : parsedSuites) {
                // If test names were specified, only run these test names
                if (testNames != null) {
                  XmlSuiteUtils xmlSuiteUitls = new XmlSuiteUtils();
                  xmlSuiteUitls.cloneIfContainsTestsWithNamesMatchingAny(suite, testNames);
                  List<String> missMatchedTestname = xmlSuiteUitls.getMissMatchedTestNames(testNames);
                  if (CollectionUtils.hasElements(missMatchedTestname)) {
                    throw new TestNGException("The test(s) <" + Arrays.toString(missMatchedTestname.toArray())+ "> cannot be found.");
                  }
                  suites.addAll(xmlSuiteUitls.getCloneSuite());
                } else {
                  suites.add(suite);
                }
                return true;
              }
        }
        return foundTestngXml;
    }
    
    private static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = "testngXmlPathInJar"+System.currentTimeMillis() + "-";
        for (int counter = 0; counter < 10; counter++) {
          File tempDir = new File(baseDir, baseName + counter);
          if (tempDir.mkdir()) {
            tempDir.deleteOnExit();
            return tempDir;
          }
        }
        throw new IllegalStateException("Failed to create directory within "
            + 5 + " attempts (tried "
            + baseName + "0 to " + baseName + (10 - 1) + ')');
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
