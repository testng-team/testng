package org.testng.remote.strprotocol;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.ITestContext;
import org.testng.ITestResult;


/**
 * An <code>IStringMessage</code> implementation for test results events.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class TestResultMessage implements IStringMessage {
  protected int    m_messageType;
  protected String m_suiteName;
  protected String m_testName;
  protected String m_testClassName;
  protected String m_testMethodName;
  protected String m_stackTrace;
  protected long m_startMillis;
  protected long m_endMillis;

  TestResultMessage(final int resultType,
                    final String suiteName,
                    final String testName,
                    final String className,
                    final String methodName,
                    final long startMillis,
                    final long endMillis,
                    final String stackTrace) 
  {
    init(resultType,
         suiteName,
         testName,
         className,
         methodName,
         stackTrace,
         startMillis,
         endMillis
    );
  }

  public TestResultMessage(final String suiteName, 
                           final String testName, 
                           final ITestResult result) 
  {
    String stackTrace = null;

    if((ITestResult.FAILURE == result.getStatus())
      || (ITestResult.SUCCESS_PERCENTAGE_FAILURE == result.getStatus())) {
      StringWriter sw = new StringWriter();
      PrintWriter  pw = new PrintWriter(sw);
      Throwable cause= result.getThrowable();
      if (null != cause) {
        cause.printStackTrace(pw);
        stackTrace = sw.getBuffer().toString();
      }
      else {
        stackTrace= "unknown stack trace";
      }
      
    }

    init(MessageHelper.TEST_RESULT + result.getStatus(),
         suiteName,
         testName,
         result.getTestClass().getName(),
         result.getMethod().getMethod().getName(),
         stackTrace,
         result.getStartMillis(),
         result.getEndMillis()
    );
  }
  
  public TestResultMessage(final ITestContext testCtx, final ITestResult result) {
    this(testCtx.getSuite().getName(), testCtx.getName(), result);
  }

  private void init(final int resultType,
                    final String suiteName,
                    final String testName,
                    final String className,
                    final String methodName,
                    final String stackTrace,
                    final long startMillis,
                    final long endMillis) {
    m_messageType = resultType;
    m_suiteName = suiteName;
    m_testName = testName;
    m_testClassName = className;
    m_testMethodName = methodName;
    m_stackTrace = stackTrace;
    m_startMillis= startMillis;
    m_endMillis= endMillis;
  }

  public int getResult() {
    return m_messageType;
  }

  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();

    buf.append(m_messageType)
       .append(MessageHelper.DELIMITER)
       .append(m_suiteName)
       .append(MessageHelper.DELIMITER)
       .append(m_testName)
       .append(MessageHelper.DELIMITER)
       .append(m_testClassName)
       .append(MessageHelper.DELIMITER)
       .append(m_testMethodName)
       .append(MessageHelper.DELIMITER)
       .append(m_startMillis)
       .append(MessageHelper.DELIMITER)
       .append(m_endMillis)
       .append(MessageHelper.DELIMITER)
       .append(MessageHelper.replaceNewLine(m_stackTrace))
       ;

    return buf.toString();
  }

  public String getSuiteName() {
    return m_suiteName;
  }

  public String getTestClass() {
    return m_testClassName;
  }

  public String getMethod() {
    return m_testMethodName;
  }

  public String getName() {
    return m_testName;
  }
  
  public String getStackTrace() {
    return m_stackTrace;
  }
  
  public long getEndMillis() {
    return m_endMillis;
  }
  
  public long getStartMillis() {
    return m_startMillis;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    final TestResultMessage that = (TestResultMessage)o;

    if(m_suiteName != null ? !m_suiteName.equals(that.m_suiteName) : that.m_suiteName != null) return false;
    if(!m_testClassName.equals(that.m_testClassName)) return false;
    if(!m_testMethodName.equals(that.m_testMethodName)) return false;
    if(m_testName != null ? !m_testName.equals(that.m_testName) : that.m_testName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (m_suiteName != null ? m_suiteName.hashCode() : 0);
    result = 29 * result + (m_testName != null ? m_testName.hashCode() : 0);
    result = 29 * result + m_testClassName.hashCode();
    result = 29 * result + m_testMethodName.hashCode();
    return result;
  }
}
