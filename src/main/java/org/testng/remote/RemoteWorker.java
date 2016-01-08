package org.testng.remote;

import org.testng.SuiteRunner;
import org.testng.internal.Utils;
import org.testng.internal.remote.SlavePool;
import org.testng.remote.adapter.RemoteResultListener;
import org.testng.xml.XmlSuite;

import java.io.IOException;

public class RemoteWorker {
  protected RemoteResultListener m_listener;
  private SlavePool m_slavePool;

  public RemoteWorker(RemoteResultListener listener, SlavePool slavePool) {
    m_listener = listener;
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
