package org.testng;

/**
 * A TestNG listener that can be used to build graph representations of TestNG methods as and when
 * they are being executed on a real-time basis.
 */
public interface IExecutionVisualiser extends ITestNGListener {
  /**
   * @param dotDefinition - A <a href="https://graphviz.gitlab.io/_pages/doc/info/lang.html">DOT</a>
   *     representation of the Directed Acyclic Graph that TestNG builds internally to represent its
   *     tests.
   */
  void consumeDotDefinition(String dotDefinition);
}
