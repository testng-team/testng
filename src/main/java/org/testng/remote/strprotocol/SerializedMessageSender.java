package org.testng.remote.strprotocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class SerializedMessageSender extends BaseMessageSender {

  private ObjectOutputStream m_oos;
  private ObjectInputStream m_ios;

  public SerializedMessageSender(String host, int port) {
    super(host, port);
  }

  private void initStreams(boolean output) {
    try {
      if (output) m_oos = new ObjectOutputStream(m_outStream);
      else m_ios = new ObjectInputStream(m_inStream);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void sendMessage(IMessage message) throws IOException {
    synchronized(m_lock) {
      m_oos = new ObjectOutputStream(m_outStream);
      p("Sending message " + message);
      m_oos.writeObject(message);
      m_oos.flush();

      try {
        p("Message sent, waiting for lock...");
        m_lock.wait();
        p("... lock done");
      }
      catch(InterruptedException e) {
      }
    }
  }


  @Override
  public IMessage receiveMessage() throws IOException, ClassNotFoundException {

    IMessage result = null;
    try {
      m_ios = new ObjectInputStream(m_inStream);
//      synchronized(m_input) {
        result = (IMessage) m_ios.readObject();
        p("Received message " + result);
//        sendAck();
//      }
    }
    catch(EOFException ex) {
      // ignore
    }
    return result;
  }

  private static void p(String s) {
    System.out.println("[SerializedMessageSender] " + s);
  }
}
