package org.testng.remote.strprotocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.collections.Lists;

import static org.testng.internal.Utils.isStringEmpty;


/**
 * An <code>IStringMessage</code> implementation for test results events.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class TestResultMessage implements IStringMessage {
  private static final long serialVersionUID = -4157150777889117479L;
  protected int    m_messageType;
  protected String m_suiteName;
  protected String m_testName;
  protected String m_testClassName;
  protected String m_testMethodName;
  protected String m_stackTrace;
  protected long m_startMillis;
  protected long m_endMillis;
  protected String[] m_parameters= new String[0];
  protected String[] m_paramTypes= new String[0];
  private String m_testDescription;
  private int m_invocationCount;
  private int m_currentInvocationCount;
  private String m_instanceName;

  /**
   * This constructor is used by the Eclipse client to initialize a result message based
   * on what was received over the network.
   */
  public TestResultMessage(final int resultType,
                    final String suiteName,
                    final String testName,
                    final String className,
                    final String methodName,
                    final String testDescriptor,
                    String instanceName,
                    final String[] params,
                    final long startMillis,
                    final long endMillis,
                    final String stackTrace,
                    int invocationCount,
                    int currentInvocationCount)
  {
    init(resultType,
         suiteName,
         testName,
         className,
         methodName,
         stackTrace,
         startMillis,
         endMillis,
         extractParams(params),
         extractParamTypes(params),
         testDescriptor,
         instanceName,
         invocationCount,
         currentInvocationCount
    );
  }

  /**
   * This constructor is used by RemoteTestNG to initialize a result message
   * from an ITestResult.
   */
  public TestResultMessage(final String suiteName,
                           final String testName,
                           final ITestResult result)
  {
    Throwable throwable = result.getThrowable();
    String stackTrace = null;

    if((ITestResult.FAILURE == result.getStatus())
      || (ITestResult.SUCCESS_PERCENTAGE_FAILURE == result.getStatus())) {
      StringWriter sw = new StringWriter();
      PrintWriter  pw = new PrintWriter(sw);
      Throwable cause= throwable;
      if (null != cause) {
        cause.printStackTrace(pw);
        stackTrace = sw.getBuffer().toString();
      }
      else {
        stackTrace= "unknown stack trace";
      }
    }
    else if(ITestResult.SKIP == result.getStatus()
        && (throwable != null && SkipException.class.isAssignableFrom(throwable.getClass()))) {
      stackTrace= throwable.getMessage();
    } else if (throwable != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      throwable.printStackTrace(pw);
      stackTrace = sw.toString();
    }

    init(MessageHelper.TEST_RESULT + result.getStatus(),
         suiteName,
         testName,
         result.getTestClass().getName(),
         result.getMethod().getMethod().getName(),
         MessageHelper.replaceUnicodeCharactersWithAscii(stackTrace),
         result.getStartMillis(),
         result.getEndMillis(),
         toString(result.getParameters(), result.getMethod().getMethod().getParameterTypes()),
         toString(result.getMethod().getMethod().getParameterTypes()),
         MessageHelper.replaceUnicodeCharactersWithAscii(result.getName()),
         MessageHelper.replaceUnicodeCharactersWithAscii(result.getInstanceName()),
         result.getMethod().getInvocationCount(),
         result.getMethod().getCurrentInvocationCount()
    );
  }

  public TestResultMessage(final ITestContext testCtx, final ITestResult result) {
    this(testCtx.getSuite().getName(), testCtx.getCurrentXmlTest().getName(), result);
//    this(testCtx.getSuite().getName(),
//        result.getTestName() != null ? result.getTestName() : result.getName(), result);
  }

  private void init(final int resultType,
                    final String suiteName,
                    final String testName,
                    final String className,
                    final String methodName,
                    final String stackTrace,
                    final long startMillis,
                    final long endMillis,
                    final String[] parameters,
                    final String[] types,
                    final String testDescription,
                    String instanceName,
                    int invocationCount,
                    int currentInvocationCount) {
    m_messageType = resultType;
    m_suiteName = suiteName;
    m_testName = testName;
    m_testClassName = className;
    m_testMethodName = methodName;
    m_stackTrace = stackTrace;
    m_startMillis= startMillis;
    m_endMillis= endMillis;
    m_parameters= parameters;
    m_paramTypes= types;
    m_testDescription= testDescription;
    m_invocationCount = invocationCount;
    m_currentInvocationCount = currentInvocationCount;
    m_instanceName = instanceName;
  }

  public int getResult() {
    return m_messageType;
  }

  @Override
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();
    StringBuffer parambuf = new StringBuffer();

    if(null != m_parameters && m_parameters.length > 0) {
      for (int j = 0; j < m_parameters.length; j++) {
        if (j > 0) {
          parambuf.append(MessageHelper.PARAM_DELIMITER);
        }
        parambuf.append(m_paramTypes[j] + ":" + m_parameters[j]);
      }
    }

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
       .append(parambuf)
       .append(MessageHelper.DELIMITER)
       .append(m_startMillis)
       .append(MessageHelper.DELIMITER)
       .append(m_endMillis)
       .append(MessageHelper.DELIMITER)
       .append(MessageHelper.replaceNewLine(m_stackTrace))
       .append(MessageHelper.DELIMITER)
       .append(MessageHelper.replaceNewLine(m_testDescription))
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

  public String[] getParameters() {
    return m_parameters;
  }

  public String[] getParameterTypes() {
    return m_paramTypes;
  }

  public String getTestDescription() {
    return m_testDescription;
  }

  public String toDisplayString() {
    StringBuffer buf= new StringBuffer(m_testName != null ? m_testName : m_testMethodName);

    if(null != m_parameters && m_parameters.length > 0) {
      buf.append("(");
      for(int i= 0; i < m_parameters.length; i++) {
        if(i > 0) {
          buf.append(", ");
        }
        if("java.lang.String".equals(m_paramTypes[i]) && !("null".equals(m_parameters[i]) || "\"\"".equals(m_parameters[i]))) {
          buf.append("\"").append(m_parameters[i]).append("\"");
        }
        else {
          buf.append(m_parameters[i]);
        }

      }
      buf.append(")");
    }

    return buf.toString();
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    final TestResultMessage that = (TestResultMessage) o;

    if(m_suiteName != null ? !m_suiteName.equals(that.m_suiteName) : that.m_suiteName != null) {
      return false;
    }
    if(m_testName != null ? !m_testName.equals(that.m_testName) : that.m_testName != null) {
      return false;
    }
    if(m_testClassName != null ? !m_testClassName.equals(that.m_testClassName) : that.m_testClassName != null) {
      return false;
    }
    String toDisplayString= toDisplayString();
    if(toDisplayString != null ? !toDisplayString.equals(that.toDisplayString()) : that.toDisplayString() != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = (m_suiteName != null ? m_suiteName.hashCode() : 0);
    result = 29 * result + (m_testName != null ? m_testName.hashCode() : 0);
    result = 29 * result + m_testClassName.hashCode();
    result = 29 * result + toDisplayString().hashCode();
    return result;
  }

  String[] toString(Object[] objects, Class<?>[] objectClasses) {
    if(null == objects) {
      return new String[0];
    }
    List<String> result= Lists.newArrayList(objects.length);
    for(Object o: objects) {
      if(null == o) {
        result.add("null");
      }
      else if (o.getClass().isArray()) {
        String[] strArray;
        if (o.getClass().getComponentType().isPrimitive()){
          strArray = primitiveArrayToString(o);
        } else {
          strArray = toString((Object[]) o, null);
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < strArray.length; i++)
        {
          sb.append(strArray[i]);
          if (i + 1 < strArray.length)
          {
            sb.append(",");
          }
        }
        sb.append("]");
        result.add(sb.toString());
      }
      else {
        String tostring= o.toString();
        if(isStringEmpty(tostring)) {
          result.add("\"\"");
        }
        else {
          result.add(MessageHelper.replaceNewLine(tostring));
        }
      }
    }

    return result.toArray(new String[result.size()]);
  }

  private String[] primitiveArrayToString(Object o) {
    List<String> results = Lists.newArrayList();
    if (o instanceof byte[]) {
      byte[] array = (byte[]) o;
        for (byte anArray : array) {
            results.add(String.valueOf(anArray));
        }
    } else if (o instanceof boolean[]) {
      boolean[] array = (boolean[]) o;
        for (boolean anArray : array) {
            results.add(String.valueOf(anArray));
        }
    } else if (o instanceof char[]) {
      char[] array = (char[]) o;
        for (char anArray : array) {
            results.add(String.valueOf(anArray));
        }
    } else if (o instanceof double[]) {
      double[] array = (double[]) o;
        for (double anArray : array) {
            results.add(String.valueOf(anArray));
        }
    } else if (o instanceof float[]) {
      float[] array = (float[]) o;
        for (float anArray : array) {
            results.add(String.valueOf(anArray));
        }
    } else if (o instanceof short[]) {
      short[] array = (short[]) o;
        for (short anArray : array) {
            results.add(String.valueOf(anArray));
        }
    } else if (o instanceof int[]) {
      int[] array = (int[]) o;
        for (int anArray : array) {
            results.add(String.valueOf(anArray));
        }
    } else if (o instanceof long[]) {
      long[] array = (long[]) o;
        for (long anArray : array) {
            results.add(String.valueOf(anArray));
        }
    }
    return results.toArray(new String[results.size()]);
  }

  private String[] toString(Class<?>[] classes) {
    if(null == classes) {
      return new String[0];
    }
    List<String> result= Lists.newArrayList(classes.length);
    for(Class<?> cls: classes) {
      result.add(cls.getName());
    }

    return result.toArray(new String[result.size()]);
  }

  private String[] extractParamTypes(String[] params) {
    List<String> result= Lists.newArrayList(params.length);
    for(String s: params) {
      result.add(s.substring(0, s.indexOf(':')));
    }

    return result.toArray(new String[result.size()]);
  }

  private String[] extractParams(String[] params) {
    List<String> result= Lists.newArrayList(params.length);
    for(String s: params) {
      result.add(MessageHelper.replaceNewLineReplacer(s.substring(s.indexOf(':') + 1)));
    }

    return result.toArray(new String[result.size()]);
  }

  public int getInvocationCount() {
    return m_invocationCount;
  }

  public int getCurrentInvocationCount() {
    return m_currentInvocationCount;
  }

  @Override
  public String toString() {
    return "[TestResultMessage suite:" + m_suiteName + " test:" + m_testName
        + " method:" + m_testMethodName
        + "]";
  }

  public void setParameters(String[] params) {
    m_parameters = extractParams(params);
    m_paramTypes = extractParamTypes(params);
  }

  public String getInstanceName() {
    return m_instanceName;
  }
}
