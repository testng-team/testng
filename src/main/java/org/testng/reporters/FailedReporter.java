package org.testng.reporters;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.MethodHelper;
import org.testng.internal.Utils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This reporter is responsible for creating testng-failed.xml
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class FailedReporter extends TestListenerAdapter implements IReporter {
  public static final String TESTNG_FAILED_XML = "testng-failed.xml";

  private XmlSuite m_xmlSuite;

  public FailedReporter() {
  }

  public FailedReporter(XmlSuite xmlSuite) {
    m_xmlSuite = xmlSuite;
  }

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    for (ISuite suite : suites) {
      generateFailureSuite(suite.getXmlSuite(), suite, outputDirectory);
    }
  }

  protected void generateFailureSuite(XmlSuite xmlSuite, ISuite suite, String outputDir) {
    XmlSuite failedSuite = (XmlSuite) xmlSuite.clone();
    failedSuite.setName("Failed suite [" + xmlSuite.getName() + "]");
    m_xmlSuite= failedSuite;

    Map<String, XmlTest> xmlTests= Maps.newHashMap();
    for(XmlTest xmlT: xmlSuite.getTests()) {
      xmlTests.put(xmlT.getName(), xmlT);
    }

    Map<String, ISuiteResult> results = suite.getResults();

    for(Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
      ISuiteResult suiteResult = entry.getValue();
      ITestContext testContext = suiteResult.getTestContext();

      generateXmlTest(suite,
                      xmlTests.get(testContext.getName()),
                      testContext,
                      testContext.getFailedTests().getAllResults(),
                      testContext.getSkippedTests().getAllResults());
    }

    if(null != failedSuite.getTests() && failedSuite.getTests().size() > 0) {
      Utils.writeUtf8File(outputDir, TESTNG_FAILED_XML, failedSuite.toXml());
      Utils.writeUtf8File(suite.getOutputDirectory(), TESTNG_FAILED_XML, failedSuite.toXml());
    }
  }

  /**
   * Do not rely on this method. The class is used as <code>IReporter</code>.
   *
   * @see org.testng.TestListenerAdapter#onFinish(org.testng.ITestContext)
   * @deprecated this class is used now as IReporter
   */
  @Deprecated
  @Override
  public void onFinish(ITestContext context) {
    // Delete the previous file
//    File f = new File(context.getOutputDirectory(), getFileName(context));
//    f.delete();

    // Calculate the methods we need to rerun :  failed tests and
    // their dependents
//    List<ITestResult> failedTests = getFailedTests();
//    List<ITestResult> skippedTests = getSkippedTests();
  }

  private void generateXmlTest(ISuite suite,
                               XmlTest xmlTest,
                               ITestContext context,
                               Collection<ITestResult> failedTests,
                               Collection<ITestResult> skippedTests) {
    // Note:  we can have skipped tests and no failed tests
    // if a method depends on nonexistent groups
    if (skippedTests.size() > 0 || failedTests.size() > 0) {
      Set<ITestNGMethod> methodsToReRun = Sets.newHashSet();

      // Get the transitive closure of all the failed methods and the methods
      // they depend on
      Collection[] allTests = new Collection[] {
          failedTests, skippedTests
      };

      for (Collection<ITestResult> tests : allTests) {
        for (ITestResult failedTest : tests) {
          ITestNGMethod current = failedTest.getMethod();
          if (current.isTest()) {
            methodsToReRun.add(current);
            ITestNGMethod method = failedTest.getMethod();
            // Don't count configuration methods
            if (method.isTest()) {
              List<ITestNGMethod> methodsDependedUpon =
                  MethodHelper.getMethodsDependedUpon(method, context.getAllTestMethods());

              for (ITestNGMethod m : methodsDependedUpon) {
                if (m.isTest()) {
                  methodsToReRun.add(m);
                }
              }
            }
          }
        }
      }

      //
      // Now we have all the right methods.  Go through the list of
      // all the methods that were run and only pick those that are
      // in the methodToReRun map.  Since the methods are already
      // sorted, we don't need to sort them again.
      //
      List<ITestNGMethod> result = Lists.newArrayList();
      for (ITestNGMethod m : context.getAllTestMethods()) {
        if (methodsToReRun.contains(m)) {
          result.add(m);
        }
      }

      methodsToReRun.clear();
      Collection<ITestNGMethod> invoked= suite.getInvokedMethods();
      for(ITestNGMethod tm: invoked) {
        if(!tm.isTest()) {
          methodsToReRun.add(tm);
        }
      }

      result.addAll(methodsToReRun);
      createXmlTest(context, result, xmlTest);
    }
  }

  /**
   * Generate testng-failed.xml
   */
  private void createXmlTest(ITestContext context, List<ITestNGMethod> methods, XmlTest srcXmlTest) {
    XmlTest xmlTest = new XmlTest(m_xmlSuite);
    xmlTest.setName(context.getName() + "(failed)");
    xmlTest.setBeanShellExpression(srcXmlTest.getExpression());
    xmlTest.setIncludedGroups(srcXmlTest.getIncludedGroups());
    xmlTest.setExcludedGroups(srcXmlTest.getExcludedGroups());
    xmlTest.setParallel(srcXmlTest.getParallel());
    xmlTest.setParameters(srcXmlTest.getLocalParameters());
    xmlTest.setJUnit(srcXmlTest.isJUnit());
    List<XmlClass> xmlClasses = createXmlClasses(methods, srcXmlTest);
    xmlTest.setXmlClasses(xmlClasses);
  }

  /**
   * @param methods The methods we want to represent
   * @param srcXmlTest 
   * @return A list of XmlClass objects (each representing a <class> tag) based
   * on the parameter methods
   */
  private List<XmlClass> createXmlClasses(List<ITestNGMethod> methods, XmlTest srcXmlTest) {
    List<XmlClass> result = Lists.newArrayList();
    Map<Class, Set<ITestNGMethod>> methodsMap= Maps.newHashMap();

    for (ITestNGMethod m : methods) {
      Object[] instances= m.getInstances();
      Class clazz= instances == null || instances.length == 0 || instances[0] == null
          ? m.getRealClass()
          : instances[0].getClass();
      Set<ITestNGMethod> methodList= methodsMap.get(clazz);
      if(null == methodList) {
        methodList= new HashSet<>();
        methodsMap.put(clazz, methodList);
      }
      methodList.add(m);
    }

    // Ideally, we should preserve each parameter in each class but putting them
    // all in the same bag for now
    Map<String, String> parameters = Maps.newHashMap();
    for (XmlClass c : srcXmlTest.getClasses()) {
      parameters.putAll(c.getLocalParameters());
    }

    int index = 0;
    for(Map.Entry<Class, Set<ITestNGMethod>> entry: methodsMap.entrySet()) {
      Class clazz= entry.getKey();
      Set<ITestNGMethod> methodList= entry.getValue();
      // @author Borojevic
      // Need to check all the methods, not just @Test ones.
      XmlClass xmlClass= new XmlClass(clazz.getName(), index++, false /* don't load classes */);
      List<XmlInclude> methodNames= Lists.newArrayList(methodList.size());
      int ind = 0;
      for(ITestNGMethod m: methodList) {
        methodNames.add(new XmlInclude(m.getMethod().getName(), m.getFailedInvocationNumbers(),
            ind++));
      }
      xmlClass.setIncludedMethods(methodNames);
      xmlClass.setParameters(parameters);
      result.add(xmlClass);

    }

    return result;
  }

  /**
   * TODO:  we might want to make that more flexible in the future, but for
   * now, hardcode the file name
   */
  private String getFileName(ITestContext context) {
    return TESTNG_FAILED_XML;
  }

  private static void ppp(String s) {
    System.out.println("[FailedReporter] " + s);
  }
}
