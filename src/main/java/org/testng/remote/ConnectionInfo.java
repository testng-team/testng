package org.testng.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionInfo {
  private Socket m_socket;
  private ObjectInputStream m_ois;
  private ObjectOutputStream m_oos;

  public ObjectInputStream getOis() throws IOException {
    if (m_ois == null) {
      m_ois = new ObjectInputStream(m_socket.getInputStream());
    }
    return m_ois;
  }

  public ObjectOutputStream getOos() throws IOException {
    if (m_oos == null) {
      m_oos = new ObjectOutputStream(m_socket.getOutputStream());
    }
    return m_oos;
  }

  public void setSocket(Socket s) {
    m_socket = s;
  }

  public Socket getSocket() {
    return m_socket;
  }

}
