package org.testng.xml;

import java.util.Collections;
import java.util.List;

public class XmlInclude {
  private String m_name;
  private List<Integer> m_invocationNumbers;

  public XmlInclude(String n) {
    this(n, Collections.<Integer>emptyList());
  }

  public XmlInclude(String n, List<Integer> list) {
    m_name = n;
    m_invocationNumbers = list;
  }

  public String getName() {
    return m_name;
  }
  
  public List<Integer> getInvocationNumbers() {
    return m_invocationNumbers;
  }

}
