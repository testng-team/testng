package org.testng.internal;

import java.util.Arrays;
import java.util.Collection;
import org.testng.xml.IPostProcessor;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/** Override the groups included in the XML file with groups specified on the command line. */
public class OverrideProcessor implements IPostProcessor {

  private final String[] m_groups;
  private final String[] m_excludedGroups;

  public OverrideProcessor(String[] groups, String[] excludedGroups) {
    m_groups = groups;
    m_excludedGroups = excludedGroups;
  }

  @Override
  public Collection<XmlSuite> process(Collection<XmlSuite> suites) {
    for (XmlSuite s : suites) {
      if (m_groups != null && m_groups.length > 0) {
        for (XmlTest t : s.getTests()) {
          t.setIncludedGroups(Arrays.asList(m_groups));
        }
      }

      if (m_excludedGroups != null && m_excludedGroups.length > 0) {
        for (XmlTest t : s.getTests()) {
          t.setExcludedGroups(Arrays.asList(m_excludedGroups));
        }
      }
    }

    return suites;
  }
}
