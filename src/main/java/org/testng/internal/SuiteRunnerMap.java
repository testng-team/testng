package org.testng.internal;

import org.testng.ISuite;
import org.testng.TestNGException;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;

import java.util.Collection;
import java.util.Map;

public class SuiteRunnerMap {

  private Map<String, ISuite> m_map = Maps.newHashMap();

  public void put(XmlSuite xmlSuite, ISuite suite) {
    final String name = xmlSuite.getName();
    if (m_map.containsKey(name)) {
      throw new TestNGException("SuiteRunnerMap already have runner for suite " + name);
    }
    m_map.put(name, suite);
  }

  public ISuite get(XmlSuite xmlSuite) {
    return m_map.get(xmlSuite.getName());
  }

  public Collection<ISuite> values() {
    return m_map.values();
  }
}
