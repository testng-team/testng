package org.testng.internal;

import org.testng.xml.IPostProcessor;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collection;

/**
 * Override the groups included in the XML file with groups specified on the command line.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class OverrideProcessor implements IPostProcessor {

  private String[] m_groups;
  private String[] m_excludedGroups;

  public OverrideProcessor(String[] groups, String[] excludedGroups) {
    m_groups = groups;
    m_excludedGroups = excludedGroups;
  }

  @Override
  public Collection<XmlSuite> process(Collection<XmlSuite> suites) {
    for (XmlSuite s : suites) {
      if (m_groups != null && m_groups.length > 0) {
        for (XmlTest t : s.getTests()) {
          t.getIncludedGroups().clear();
          t.getIncludedGroups().addAll(Arrays.asList(m_groups));
        }
      }

      if (m_excludedGroups != null && m_excludedGroups.length > 0) {
        for (XmlTest t : s.getTests()) {
          t.getExcludedGroups().clear();
          t.getExcludedGroups().addAll(Arrays.asList(m_excludedGroups));
        }
      }
    }

    return suites;
  }

}
