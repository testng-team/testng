package org.testng.xml;

import java.util.Collections;
import java.util.List;

import com.google.inject.internal.Lists;

public class XmlInclude {
  private String m_name;
  private List<Integer> m_invocationNumbers = Lists.newArrayList();
  private int m_index;

  public XmlInclude(String n) {
    this(n, Collections.<Integer>emptyList(), 0);
  }

  public XmlInclude(String n, int index) {
    this(n, Collections.<Integer>emptyList(), index);
  }

  public XmlInclude(String n, List<Integer> list, int index) {
    m_name = n;
    m_invocationNumbers = list;
    m_index = index;
  }

  public String getName() {
    return m_name;
  }
  
  public List<Integer> getInvocationNumbers() {
    return m_invocationNumbers;
  }

  public int getIndex() {
    return m_index;
  }

}
