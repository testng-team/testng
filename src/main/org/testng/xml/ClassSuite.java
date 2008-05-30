package org.testng.xml;

import java.util.Arrays;

import org.testng.SuiteRunner;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.ClassHelper;
import org.testng.internal.Utils;
import org.testng.internal.annotations.AnnotationConfiguration;
import org.testng.internal.annotations.IAnnotationFinder;

/**
 * This class represents an XML <code>&lt;suite&gt;</code> made up of one test which is
 * made up of &lt;class&gt; elements only. Given a testName "testName" it is equivalent to the
 * following XML &lt;suite&gt;:
 *
 * <pre><code>
 *    &lt;suite name ="Suite for testName"&gt;
 *       &lt;test name ="testName"&gt;
 *          &lt;classes&gt;
 *              ...
 *          &lt;/classes&gt;
 *       &lt;/test&gt;
 *    &lt;/suite&gt;
 * </code></pre>
 *
 * This class is typically used to build a XML &lt;suite&gt; from command line class parameters.
 * @author jolly
 */

// TODO CQ why does this class exist? Should'nt this be a constructor in XmlSuite, a
// factory helper or a simple class not extending XmlSuite outside this package?
// The code uses instanceof test on this class to set the annotation type.

public class ClassSuite extends XmlSuite {

    /**
     * Constructs a <code>XmlSuite</code>. The suite has the following characteristics:
     * <ul>
     * <li>The suite name is "Suite for testName"</li>
     * <li>The suite is made up of a single test named "testName"</li>
     * <li>The test is made up of list &lt;class&gt; only</li>
     * </ul>
     *
     * @param testName the suite and inner test name.
     * @param classes the classes making up the suite test.
     */
    public ClassSuite(String testName, Class[] classes) {
        super();
        XmlClass[] xmlClasses = Utils.classesToXmlClasses(classes);
        XmlTest oneTest = new XmlTest(this);
        oneTest.setName(testName);
        oneTest.setXmlClasses(Arrays.asList(xmlClasses));
        Class c = classes[0];
        setName("Suite for " + testName);  // Set the suite name to the test name, too
    }
}
