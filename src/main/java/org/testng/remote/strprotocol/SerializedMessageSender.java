package org.testng.remote.strprotocol;

import java.io.IOException;
import java.io.ObjectOutputStream;


public class SerializedMessageSender extends BaseMessageSender {

  public SerializedMessageSender(String host, int port) {
    super(host, port);
  }

  @Override
  public void sendMessage(IMessage message) throws IOException {
    ObjectOutputStream oos = new ObjectOutputStream(m_outStream);

    synchronized(m_lock) {
      oos.writeObject(message);
      oos.flush();
      try {
        m_lock.wait();
      }
      catch(InterruptedException e) { }
    }
  }

  @Override
  public IMessage receiveMessage() {
    return null;
  }
}
