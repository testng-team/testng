package org.testng.remote.strprotocol;


import org.testng.TestNGException;
import org.testng.remote.RemoteTestNG;

import java.io.IOException;
import java.util.List;

/**
 * Central class to connect to the host and send message.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class MessageHub {

  private boolean m_debug = false;

  private List<IMessageSender> m_messageSenders;

  public MessageHub(List<IMessageSender> messageSenders) {
    m_messageSenders = messageSenders;
  }

  /**
   * Starts the connection.
   *
   * @return <TT>true</TT> if the connection was successful, <TT>false</TT> otherwise
   * @throws TestNGException if an exception occurred while establishing the connection
   */
  public void connect() throws IOException {
    for (IMessageSender sender : m_messageSenders) {
      sender.connect();
    }
  }

  /**
   * Shutsdown the connection to the remote test listener.
   */
  public void shutDown() {
    for (IMessageSender sender : m_messageSenders) {
      sender.shutDown();
    }
  }

  public void sendMessage(IMessage message) {
    for (IMessageSender sender : m_messageSenders) {
      try {
        sender.sendMessage(message);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private static void p(String msg) {
    if (RemoteTestNG.isVerbose()) {
      System.out.println("[StringMessageSenderHelper] " + msg); //$NON-NLS-1$
    }
  }


  public void setDebug(boolean debug) {
    m_debug = debug;
  }
}
