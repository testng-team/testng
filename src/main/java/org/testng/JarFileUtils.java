package org.testng;

import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.xml.IPostProcessor;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.XmlSuiteUtils;

import java.io.File;
import java.io.IOException;
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
        this.testNames = testNames;
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
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                if (matchesXmlPathInJar(je)) {
                    Collection<XmlSuite> parsedSuites = Parser.parse(jf.getInputStream(je), processor);
                    for (XmlSuite suite : parsedSuites) {
                        // If test names were specified, only run these test names
                        XmlSuite suiteToAdd = suite;
                        if (testNames != null) {
                            suiteToAdd = XmlSuiteUtils.cloneIfContainsTestsWithNamesMatchingAny(suite, testNames);
                        }
                        suites.add(suiteToAdd);
                    }
                    return true;
                } else if (isJavaClass(je)) {
                    classes.add(constructClassName(je));
                }
            }
        }
        return foundTestngXml;
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
