package org.testng.remote;

import java.util.List;

import org.testng.ISuite;
import org.testng.SuiteRunner;
import org.testng.internal.remote.SlavePool;
import org.testng.xml.XmlSuite;

/**
 * A worker that will be put into an Executor and that sends a suite
 * This class
 * 
 * @author cbeust
 */
public class RemoteSuiteWorker extends RemoteWorker implements Runnable {
  private XmlSuite m_suite;
  
  public RemoteSuiteWorker(XmlSuite suite, SlavePool slavePool, List<ISuite> result) {
    super(result, slavePool);
    m_suite = suite;
  }
  
  public void run() {
    try {
      SuiteRunner result = sendSuite(getSlavePool().getSlave(), m_suite);
      m_result.add(result);
    }
    catch (Exception e) {
      e.printStackTrace();
    }    

  }
}

