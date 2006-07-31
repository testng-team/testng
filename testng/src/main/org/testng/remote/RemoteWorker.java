package org.testng.remote;

import java.io.IOException;
import java.util.List;

import org.testng.ISuite;
import org.testng.SuiteRunner;
import org.testng.internal.Utils;
import org.testng.internal.remote.SlavePool;
import org.testng.xml.XmlSuite;

public class RemoteWorker {
  protected List<ISuite> m_result;
  private SlavePool m_slavePool;

  public RemoteWorker(List<ISuite> result, SlavePool slavePool) {
    m_result = result;
    m_slavePool = slavePool;
  }
  
  protected SlavePool getSlavePool() {
    return m_slavePool;
  }
  
  protected SuiteRunner sendSuite(ConnectionInfo ci, XmlSuite suite)
    throws IOException, ClassNotFoundException 
  {
    log("Sending " + suite.getName() + " to " 
        + ci.getSocket().getInetAddress().getCanonicalHostName() + ":"
        + ci.getSocket().getRemoteSocketAddress());
    ci.getOos().writeObject(suite);
    ci.getOos().flush();
    SuiteRunner result = (SuiteRunner) ci.getOis().readObject();
    log("Received results for " + result.getName());
    return result;
  }
  
  private void log(String string) {
    Utils.log("", 2, string);
  }


}
