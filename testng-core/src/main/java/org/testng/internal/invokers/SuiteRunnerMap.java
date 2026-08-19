package org.testng.internal.invokers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.testng.ISuite;
import org.testng.TestNGException;
import org.testng.xml.XmlSuite;

public class SuiteRunnerMap {

  private final Map<String, ISuite> m_map = new HashMap<>();

  public void put(XmlSuite xmlSuite, ISuite suite) {
    final String name = xmlSuite.getName();
    if (m_map.containsKey(name)) {
      throw new TestNGException("SuiteRunnerMap already have runner for suite " + name);
    }
    m_map.put(name, suite);
  }

  public @Nullable ISuite get(XmlSuite xmlSuite) {
    return m_map.get(xmlSuite.getName());
  }

  /**
   * @param xmlSuite The suite to look a runner up for.
   * @return Its runner, which {@code createSuiteRunners} put here before the map was walked.
   */
  public ISuite require(XmlSuite xmlSuite) {
    return Objects.requireNonNull(get(xmlSuite), "every suite has a runner in the map");
  }

  public Collection<ISuite> values() {
    return m_map.values();
  }
}
