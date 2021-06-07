package org.testng.internal.thread.graph;

import java.util.List;
import org.testng.ISuite;
import org.testng.SuiteRunnerWorker;
import org.testng.collections.Lists;
import org.testng.internal.invokers.SuiteRunnerMap;
import org.testng.thread.IThreadWorkerFactory;
import org.testng.thread.IWorker;

/**
 * An {@code IThreadWorkerFactory} for {@code SuiteRunner}s
 *
 * @author nullin
 */
public class SuiteWorkerFactory implements IThreadWorkerFactory<ISuite> {
  private Integer m_verbose;
  private String m_defaultSuiteName;
  private SuiteRunnerMap m_suiteRunnerMap;

  public SuiteWorkerFactory(
      SuiteRunnerMap suiteRunnerMap, Integer verbose, String defaultSuiteName) {
    m_suiteRunnerMap = suiteRunnerMap;
    m_verbose = verbose;
    m_defaultSuiteName = defaultSuiteName;
  }

  /**
   * For each suite, creates a {@code SuiteRunnerWorker}
   *
   * @param suites set of suite runners
   * @return list of suite runner workers
   */
  @Override
  public List<IWorker<ISuite>> createWorkers(List<ISuite> suites) {
    List<IWorker<ISuite>> suiteWorkers = Lists.newArrayList();
    for (ISuite suiteRunner : suites) {
      SuiteRunnerWorker worker =
          new SuiteRunnerWorker(suiteRunner, m_suiteRunnerMap, m_verbose, m_defaultSuiteName);
      suiteWorkers.add(worker);
    }
    return suiteWorkers;
  }
}
