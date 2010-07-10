package org.testng.remote;


import java.util.List;
import java.util.Map;

import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;
import org.testng.TestNGCommandLineArgs;
import org.testng.TestRunner;
import org.testng.remote.strprotocol.GenericMessage;
import org.testng.remote.strprotocol.MessageHelper;
import org.testng.remote.strprotocol.RemoteMessageSenderTestListener;
import org.testng.remote.strprotocol.StringMessageSenderHelper;
import org.testng.remote.strprotocol.SuiteMessage;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Extension of TestNG registering a remote TestListener.
 * <p>
 * <i>Developer note</i>: be aware that a copy of this source is distributed along with the
 * Eclipse plugin to assure backward compatibility.
 * </p>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class RemoteTestNG extends TestNG {
  private static final String LOCALHOST= "127.0.0.1";

  private ITestRunnerFactory m_customTestRunnerFactory;
  protected String m_host;
  protected int m_port;

  public void setConnectionParameters(String host, int port) {
    if((null == host) || "".equals(host)) {
      m_host= LOCALHOST;
    }
    else {
      m_host= host;
    }

    m_port= port;
  }

  public void configure(Map cmdLineArgs) {
    super.configure(cmdLineArgs);
    setConnectionParameters((String) cmdLineArgs.get(TestNGCommandLineArgs.HOST_COMMAND_OPT),
                            Integer.parseInt((String) cmdLineArgs.get(TestNGCommandLineArgs.PORT_COMMAND_OPT)));
  }

  public void run() {
    final StringMessageSenderHelper msh= new StringMessageSenderHelper(m_host, m_port);
    try {
      if(msh.connect()) {
        if(m_suites.size() > 0) {

          int testCount= 0;

          for(int i= 0; i < m_suites.size(); i++) {
            testCount+= ((XmlSuite) m_suites.get(i)).getTests().size();
          }

          GenericMessage gm= new GenericMessage(MessageHelper.GENERIC_SUITE_COUNT);
          gm.addProperty("suiteCount", m_suites.size()).addProperty("testCount", testCount);
          msh.sendMessage(gm);

          addListener(new RemoteSuiteListener(msh));
          setTestRunnerFactory(new DelegatingTestRunnerFactory(buildTestRunnerFactory(), msh));

          super.run();
        }
        else {
          System.err.println("No test suite found.  Nothing to run");
        }
      }
      else {
        System.err.println("Cannot connect to " + m_host + " on " + m_port);
      }
    }
    catch(Throwable cause) {
      cause.printStackTrace(System.err);
    }
    finally {
      msh.shutDown();
      System.exit(0);
    }
  }

  /**
   * Override by the plugin if you need to configure differently the <code>TestRunner</code>
   * (usually this is needed if different listeners/reporters are needed).
   * <b>Note</b>: you don't need to worry about the wiring listener, because it is added
   * automatically.
   */
  protected ITestRunnerFactory buildTestRunnerFactory() {
    if(null == m_customTestRunnerFactory) {
      m_customTestRunnerFactory= new ITestRunnerFactory() {
          public TestRunner newTestRunner(ISuite suite, XmlTest xmlTest,
              List<IInvokedMethodListener> listeners) {
            TestRunner runner =
              new TestRunner(suite, xmlTest, false /*skipFailedInvocationCounts */,
              listeners);
            if (m_useDefaultListeners) {
              runner.addListener(new TestHTMLReporter());
              runner.addListener(new JUnitXMLReporter());
            }

            return runner;
          }
        };
    }

    return m_customTestRunnerFactory;
  }

  public static void main(String[] args) {
    Map commandLineArgs= TestNGCommandLineArgs.parseCommandLine(args);

    RemoteTestNG testNG= new RemoteTestNG();
    testNG.configure(commandLineArgs);
    testNG.initializeSuitesAndJarFile();
    testNG.run();
  }

  /** A ISuiteListener wiring the results using the internal string-based protocol. */
  private static class RemoteSuiteListener implements ISuiteListener {
    private final StringMessageSenderHelper m_messageSender;

    RemoteSuiteListener(StringMessageSenderHelper smsh) {
      m_messageSender= smsh;
    }

    public void onFinish(ISuite suite) {
      m_messageSender.sendMessage(new SuiteMessage(suite, false /*start*/));
    }

    public void onStart(ISuite suite) {
      m_messageSender.sendMessage(new SuiteMessage(suite, true /*start*/));
    }
  }

  private static class DelegatingTestRunnerFactory implements ITestRunnerFactory {
    private final ITestRunnerFactory m_delegateFactory;
    private final StringMessageSenderHelper m_messageSender;

    DelegatingTestRunnerFactory(ITestRunnerFactory trf, StringMessageSenderHelper smsh) {
      m_delegateFactory= trf;
      m_messageSender= smsh;
    }

    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        List<IInvokedMethodListener> listeners) {
      TestRunner tr = m_delegateFactory.newTestRunner(suite, test, listeners);
      tr.addListener(new RemoteMessageSenderTestListener(suite, test, m_messageSender));
      return tr;
    }
  }
}
