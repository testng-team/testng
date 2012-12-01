package org.testng.reporters.jq;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.SetMultiMap;
import org.testng.internal.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Model {
  private ListMultiMap<ISuite, ITestResult> m_model = Maps.newListMultiMap();
  private List<ISuite> m_suites = null;
  private Map<String, String> m_testTags = Maps.newHashMap();
  private Map<ITestResult, String> m_testResultMap = Maps.newHashMap();
  private Map<ISuite, ResultsByClass> m_failedResultsByClass = Maps.newHashMap();
  private Map<ISuite, ResultsByClass> m_skippedResultsByClass = Maps.newHashMap();
  private Map<ISuite, ResultsByClass> m_passedResultsByClass = Maps.newHashMap();
  private List<ITestResult> m_allFailedResults = Lists.newArrayList();
  // Each suite is mapped to failed.png, skipped.png or nothing (which means passed.png)
  private Map<String, String> m_statusBySuiteName = Maps.newHashMap();
  private SetMultiMap<String, String> m_groupsBySuiteName = Maps.newSetMultiMap();
  private SetMultiMap<String, String> m_methodsByGroup = Maps.newSetMultiMap();

  public Model(List<ISuite> suites) {
    m_suites = suites;
    init();
  }

  public List<ISuite> getSuites() {
    return m_suites;
  }

  private void init() {
    int testCounter = 0;
    for (ISuite suite : m_suites) {
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
          updateGroups(suite, tr);
        }
        m_passedResultsByClass.put(suite, rbc);
      }

      // Skipped
      {
        ResultsByClass rbc = new ResultsByClass();
        for (ITestResult tr : skipped) {
          m_statusBySuiteName.put(suite.getName(), "skipped");
          rbc.addResult(tr.getTestClass().getRealClass(), tr);
          updateGroups(suite, tr);
        }
        m_skippedResultsByClass.put(suite, rbc);
      }

      // Failed
      {
        ResultsByClass rbc = new ResultsByClass();
        for (ITestResult tr : failed) {
          m_statusBySuiteName.put(suite.getName(), "failed");
          rbc.addResult(tr.getTestClass().getRealClass(), tr);
          m_allFailedResults.add(tr);
          updateGroups(suite, tr);
        }
        m_failedResultsByClass.put(suite, rbc);
      }

      m_model.putAll(suite, failed);
      m_model.putAll(suite, skipped);
      m_model.putAll(suite, passed);
    }
  }

  private void updateGroups(ISuite suite, ITestResult tr) {
    String[] groups = tr.getMethod().getGroups();
    m_groupsBySuiteName.putAll(suite.getName(),
        Arrays.asList(groups));
    for (String group : groups) {
      m_methodsByGroup.put(group, tr.getMethod().getMethodName());
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

  public String getTag(ITestResult tr) {
    return m_testResultMap.get(tr);
  }

  public List<ITestResult> getTestResults(ISuite suite) {
    return nonnullList(m_model.get(suite));
   }

  public static String getTestResultName(ITestResult tr) {
    StringBuilder result = new StringBuilder(tr.getMethod().getMethodName());
    Object[] parameters = tr.getParameters();
    if (parameters.length > 0) {
      result.append("(");
      StringBuilder p = new StringBuilder();
      for (int i = 0; i < parameters.length; i++) {
        if (i > 0) p.append(", ");
        p.append(Utils.toString(parameters[i]));
      }
      if (p.length() > 100) {
        String s = p.toString().substring(0, 100);
        s = s + "...";
        result.append(s);
      } else {
        result.append(p.toString());
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

  public String getStatusForSuite(String suiteName) {
    String result = m_statusBySuiteName.get(suiteName);
    return result != null ? result : "passed";
  }

  public <T> Set<T> nonnullSet(Set<T> l) {
    return l != null ? l : Collections.<T>emptySet();
  }

  public <T> List<T> nonnullList(List<T> l) {
    return l != null ? l : Collections.<T>emptyList();
  }

  public List<String> getGroups(String name) {
    List<String> result = Lists.newArrayList(nonnullSet(m_groupsBySuiteName.get(name)));
    Collections.sort(result);
    return result;
  }

  public List<String> getMethodsInGroup(String groupName) {
    List<String> result = Lists.newArrayList(nonnullSet(m_methodsByGroup.get(groupName)));
    Collections.sort(result);
    return result;
  }

  public List<ITestResult> getAllTestResults(ISuite suite) {
    return getAllTestResults(suite, true /* tests only */);
  }

  public List<ITestResult> getAllTestResults(ISuite suite, boolean testsOnly) {
    List<ITestResult> result = Lists.newArrayList();
    for (ISuiteResult sr : suite.getResults().values()) {
      result.addAll(sr.getTestContext().getPassedTests().getAllResults());
      result.addAll(sr.getTestContext().getFailedTests().getAllResults());
      result.addAll(sr.getTestContext().getSkippedTests().getAllResults());
      if (! testsOnly) {
        result.addAll(sr.getTestContext().getPassedConfigurations().getAllResults());
        result.addAll(sr.getTestContext().getFailedConfigurations().getAllResults());
        result.addAll(sr.getTestContext().getSkippedConfigurations().getAllResults());
      }
    }
    return result;
  }
}
