package org.testng.internal.remote;

import org.testng.collections.Maps;
import org.testng.remote.ConnectionInfo;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;


/**
 * This class maintains a pool of slaves (represented by sockets).
 *
 * @author cbeust
 */
public class SlavePool {
  private static SocketLinkedBlockingQueue m_hosts = new SocketLinkedBlockingQueue();
  private static Map<Socket, ConnectionInfo> m_connectionInfos = Maps.newHashMap();

  public void addSlaves(Socket[] slaves) throws IOException {
    for (Socket s : slaves) {
      addSlave(s);
    }
  }

  public void addSlave(Socket s) {
	  if( s==null) {
      return;
    }
	  ConnectionInfo ci = new ConnectionInfo();
	  ci.setSocket(s);
	  addSlave(s, ci);
  }

  private void addSlave(Socket s, ConnectionInfo ci) {
    m_hosts.add(s);
    m_connectionInfos.put(s, ci);
  }

  public ConnectionInfo getSlave() {
    ConnectionInfo result = null;
    Socket host = null;

    try {
      host = m_hosts.take();
      result = m_connectionInfos.get(host);
    }
    catch (InterruptedException handled) {
      handled.printStackTrace();
      Thread.currentThread().interrupt();
    }

    return result;
  }

  public void returnSlave(ConnectionInfo slave) throws IOException {
    m_hosts.add(slave.getSocket());
//    ConnectionInfo ci = m_connectionInfos.remove(slave.socket);
//    ci.oos.close();
//    ci.ois.close();
//    addSlave(slave.socket);
  }

}
