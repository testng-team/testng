package org.testng.remote;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.testng.CommandLineArgs;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;
import org.testng.TestRunner;
import org.testng.collections.Lists;
import org.testng.remote.strprotocol.GenericMessage;
import org.testng.remote.strprotocol.MessageHelper;
import org.testng.remote.strprotocol.RemoteMessageSenderTestListener;
import org.testng.remote.strprotocol.StringMessageSenderHelper;
import org.testng.remote.strprotocol.SuiteMessage;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;

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

  @Override
  public void configure(CommandLineArgs args) {
    super.configure(args);
    setConnectionParameters(args.host, args.port);
    System.out.println("Setting connection parameters:" + m_host + ":" + m_port);
  }

  private void calculateAllSuites(List<XmlSuite> suites, List<XmlSuite> outSuites) {
    for (XmlSuite s : suites) {
      outSuites.add(s);
      calculateAllSuites(s.getChildSuites(), outSuites);
    }
  }

  @Override
  public void run() {
    final StringMessageSenderHelper msh= new StringMessageSenderHelper(m_host, m_port);
    try {
      if(msh.connect()) {
        List<XmlSuite> suites = Lists.newArrayList();
        calculateAllSuites(m_suites, suites);
        if(suites.size() > 0) {

          int testCount= 0;

          for(int i= 0; i < suites.size(); i++) {
            testCount+= (suites.get(i)).getTests().size();
          }

          GenericMessage gm= new GenericMessage(MessageHelper.GENERIC_SUITE_COUNT);
          gm.addProperty("suiteCount", suites.size()).addProperty("testCount", testCount);
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
          @Override
          public TestRunner newTestRunner(ISuite suite, XmlTest xmlTest,
              List<IInvokedMethodListener> listeners) {
            TestRunner runner =
              new TestRunner(getConfiguration(), suite, xmlTest,
                  false /*skipFailedInvocationCounts */,
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

  public static void main(String[] args) throws ParameterException {
    CommandLineArgs cla = new CommandLineArgs();
    new JCommander(cla, args);
    validateCommandLineParameters(cla);
    RemoteTestNG testNG = new RemoteTestNG();
    testNG.configure(cla);
    testNG.initializeSuitesAndJarFile();
    testNG.run();
  }

  /** A ISuiteListener wiring the results using the internal string-based protocol. */
  private static class RemoteSuiteListener implements ISuiteListener {
    private final StringMessageSenderHelper m_messageSender;

    RemoteSuiteListener(StringMessageSenderHelper smsh) {
      m_messageSender= smsh;
    }

    @Override
    public void onFinish(ISuite suite) {
      m_messageSender.sendMessage(new SuiteMessage(suite, false /*start*/));
    }

    @Override
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

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        List<IInvokedMethodListener> listeners) {
      TestRunner tr = m_delegateFactory.newTestRunner(suite, test, listeners);
      tr.addListener(new RemoteMessageSenderTestListener(suite, test, m_messageSender));
      return tr;
    }
  }
}
