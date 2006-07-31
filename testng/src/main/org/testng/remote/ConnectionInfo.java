package org.testng.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionInfo {
  private Socket socket;
  private ObjectInputStream ois;
  private ObjectOutputStream oos;
  
  public ObjectInputStream getOis() throws IOException {
    if (ois == null) {
      ois = new ObjectInputStream(socket.getInputStream());
    }
    return ois;
  }
  
  public ObjectOutputStream getOos() throws IOException {
    if (oos == null) {
      oos = new ObjectOutputStream(socket.getOutputStream());
    }
    return oos;
  }

  public void setSocket(Socket s) {
    socket = s;
  }
  
  public Socket getSocket() {
    return socket;
  }

}
