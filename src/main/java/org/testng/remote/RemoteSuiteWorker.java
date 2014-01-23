package org.testng.remote;

import org.testng.SuiteRunner;
import org.testng.internal.remote.SlavePool;
import org.testng.remote.adapter.RemoteResultListener;
import org.testng.xml.XmlSuite;

/**
 * A worker that will be put into an Executor and that sends a suite
 * This class
 *
 * @author cbeust
 */
public class RemoteSuiteWorker extends RemoteWorker implements Runnable {
  private XmlSuite m_suite;

  public RemoteSuiteWorker(XmlSuite suite, SlavePool slavePool, RemoteResultListener listener) {
    super(listener, slavePool);
    m_suite = suite;
  }

  @Override
  public void run() {
    try {
      SuiteRunner result = sendSuite(getSlavePool().getSlave(), m_suite);
      m_listener.onResult(result);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }
}

