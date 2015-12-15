package org.testng.remote.strprotocol;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Maps;


/**
 * A <code>IStringMessage</code> implementation for suite running events.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class SuiteMessage implements IStringMessage {
  private static final long serialVersionUID = -4298528261942620419L;
  protected final String m_suiteName;
  protected final int m_testMethodCount;
  protected final boolean m_startSuite;
  private List<String> m_excludedMethods = Lists.newArrayList();
  private Map<String, String> m_descriptions;

  public SuiteMessage(final String suiteName, final boolean startSuiteRun, final int methodCount) {
    m_suiteName = suiteName;
    m_startSuite = startSuiteRun;
    m_testMethodCount = methodCount;
  }

  public SuiteMessage(final ISuite suite, final boolean startSuiteRun) {
    m_suiteName = suite.getName();
    m_testMethodCount =suite.getInvokedMethods().size();
    m_startSuite = startSuiteRun;
    Collection<ITestNGMethod> excludedMethods = suite.getExcludedMethods();
    if (excludedMethods != null && excludedMethods.size() > 0) {
      m_excludedMethods = Lists.newArrayList();
      m_descriptions = Maps.newHashMap();
      for (ITestNGMethod m : excludedMethods) {
        String methodName = m.getTestClass().getName() + "." + m.getMethodName();
        m_excludedMethods.add(methodName);
        if (m.getDescription() != null) m_descriptions.put(methodName, m.getDescription());
      }
    }
  }

  public void setExcludedMethods(List<String> methods) {
    m_excludedMethods = Lists.newArrayList();
    m_excludedMethods.addAll(methods);
  }

  public List<String> getExcludedMethods() {
    return m_excludedMethods;
  }

  public String getDescriptionForMethod(String methodName) {
    return m_descriptions.get(methodName);
  }

  public boolean isMessageOnStart() {
    return m_startSuite;
  }

  public String getSuiteName() {
    return m_suiteName;
  }

  public int getTestMethodCount() {
    return m_testMethodCount;
  }

  @Override
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();

    buf.append(m_startSuite ? MessageHelper.SUITE_START : MessageHelper.SUITE_FINISH)
        .append(MessageHelper.DELIMITER)
        .append(m_suiteName)
        .append(MessageHelper.DELIMITER)
        .append(m_testMethodCount)
        ;

    if (m_excludedMethods != null && m_excludedMethods.size() > 0) {
      buf.append(MessageHelper.DELIMITER);
      buf.append(m_excludedMethods.size());
      for (String method : m_excludedMethods) {
        buf.append(MessageHelper.DELIMITER);
        buf.append(method);
      }
    }
    return buf.toString();
  }

  @Override
  public String toString() {
    return "[SuiteMessage suite:" + m_suiteName
        + (m_startSuite ? " starting" : " ending")
        + " methodCount:" + m_testMethodCount
        + "]";
  }

}
