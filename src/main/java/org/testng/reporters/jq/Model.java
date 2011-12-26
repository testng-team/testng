package org.testng.reporters.jq;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.List;
import java.util.Map;

public class Model {
  private ListMultiMap<ISuite, ITestResult> m_model = Maps.newListMultiMap();
  private List<ISuite> m_suites = null;
  private Map<ISuite, String> m_suiteTags = Maps.newHashMap();
  private Map<String, String> m_testTags = Maps.newHashMap();
  private Map<ITestResult, String> m_testResultMap = Maps.newHashMap();
  private Map<ISuite, ResultsByClass> m_failedResultsByClass = Maps.newHashMap();
  private Map<ISuite, ResultsByClass> m_skippedResultsByClass = Maps.newHashMap();
  private Map<ISuite, ResultsByClass> m_passedResultsByClass = Maps.newHashMap();
  private List<ITestResult> m_allFailedResults = Lists.newArrayList();
  // Each suite is mapped to failed.png, skipped.png or nothing (which means passed.png)
  private Map<String, String> m_imageBySuiteName = Maps.newHashMap();

  public Model(List<ISuite> suites) {
    m_suites = suites;
    init();
  }

  public List<ISuite> getSuites() {
    return m_suites;
  }

  private void init() {
    int counter = 0;
    int testCounter = 0;
    for (ISuite suite : m_suites) {
      m_suiteTags.put(suite, "suite-" + counter++);
      List<ITestResult> passed = Lists.newArrayList();
      List<ITestResult> failed = Lists.newArrayList();
      List<ITestResult> skipped = Lists.newArrayList();
      for (ISuiteResult sr : suite.getResults().values()) {
        ITestContext context = sr.getTestContext();
        m_testTags.put(context.getName(), "test-" + testCounter++);
        failed.addAll(context.getFailedTests().getAllResults());
        skipped.addAll(context.getSkippedTests().getAllResults());
        passed.addAll(context.getPassedTests().getAllResults());
        IResultMap[] map = new IResultMap[] {
            context.getFailedTests(),
            context.getSkippedTests(),
            context.getPassedTests()
        };
        for (IResultMap m : map) {
          for (ITestResult tr : m.getAllResults()) {
            m_testResultMap.put(tr, getTestResultName(tr));
          }
        }
      }

      // Process them in the order passed, skipped and failed, so that the failed
      // icon overrides all the others and the skipped icon overrides passed.

      // Passed
      {
        ResultsByClass rbc = new ResultsByClass();
        for (ITestResult tr : passed) {
          rbc.addResult(tr.getTestClass().getRealClass(), tr);
        }
        m_passedResultsByClass.put(suite, rbc);
      }

      // Skipped
      {
        ResultsByClass rbc = new ResultsByClass();
        for (ITestResult tr : skipped) {
          m_imageBySuiteName.put(suite.getName(), getImage("skipped"));
          rbc.addResult(tr.getTestClass().getRealClass(), tr);
        }
        m_skippedResultsByClass.put(suite, rbc);
      }

      // Failed
      {
        ResultsByClass rbc = new ResultsByClass();
        for (ITestResult tr : failed) {
          m_imageBySuiteName.put(suite.getName(), getImage("failed"));
          rbc.addResult(tr.getTestClass().getRealClass(), tr);
          m_allFailedResults.add(tr);
        }
        m_failedResultsByClass.put(suite, rbc);
      }

      m_model.putAll(suite, failed);
      m_model.putAll(suite, skipped);
      m_model.putAll(suite, passed);
    }
  }

  public ResultsByClass getFailedResultsByClass(ISuite suite) {
    return m_failedResultsByClass.get(suite);
  }

  public ResultsByClass getSkippedResultsByClass(ISuite suite) {
    return m_skippedResultsByClass.get(suite);
  }

  public ResultsByClass getPassedResultsByClass(ISuite suite) {
    return m_passedResultsByClass.get(suite);
  }
  public String getTag(ISuite s) {
    return m_suiteTags.get(s);
  }

  public String getTag(ITestResult tr) {
    return m_testResultMap.get(tr);
  }

  public List<ITestResult> getTestResults(ISuite suite) {
    return m_model.get(suite);
   }

  public static String getTestResultName(ITestResult tr) {
    StringBuilder result = new StringBuilder(tr.getMethod().getMethodName());
    Object[] parameters = tr.getParameters();
    if (parameters.length > 0) {
      result.append("(");
      for (int i = 0; i < parameters.length; i++) {
        if (i > 0) result.append(", ");
        result.append(parameters[i] != null ? parameters[i].toString() : "null");
      }
      result.append(")");
    }

    return result.toString();
  }

  public List<ITestResult> getAllFailedResults() {
    return m_allFailedResults;
  }

  public static String getImage(String tagClass) {
    return tagClass + ".png";
  }

  public String getImageForSuite(String suiteName) {
    String result = m_imageBySuiteName.get(suiteName);
    if (result == null) {
      result = getImage("passed");
    }
    return result;
  }
}
