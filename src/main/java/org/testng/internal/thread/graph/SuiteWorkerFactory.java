package org.testng.internal.thread.graph;

import org.testng.ISuite;
import org.testng.SuiteRunnerWorker;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An {@code IThreadWorkerFactory} for {@code SuiteRunner}s
 *
 * @author nullin
 *
 */
public class SuiteWorkerFactory implements IThreadWorkerFactory<ISuite>
{
  private Integer m_verbose;
  private String m_defaultSuiteName;
  private Map<XmlSuite, ISuite> m_suiteRunnerMap;

  public SuiteWorkerFactory(Map<XmlSuite, ISuite> suiteRunnerMap, 
      Integer verbose, String defaultSuiteName) {
    m_suiteRunnerMap = suiteRunnerMap;
    m_verbose = verbose;
    m_defaultSuiteName = defaultSuiteName;
  }

  /**
   * For each suite, creates a {@code SuiteRunnerWorker}
   * @param suites set of suite runners
   * @return list of suite runner workers
   */
  @Override
  public List<IWorker<ISuite>> createWorkers(Set<ISuite> suites)
  {
    List<IWorker<ISuite>> suiteWorkers = Lists.newArrayList();
    for (ISuite suiteRunner : suites) {
      suiteWorkers.add(new SuiteRunnerWorker(suiteRunner, m_suiteRunnerMap, 
        m_verbose, m_defaultSuiteName));
    }
    return suiteWorkers;
  }

}
