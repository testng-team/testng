package org.testng.remote.strprotocol;

import org.testng.remote.RemoteTestNG;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class StringMessageSender extends BaseMessageSender {

  public StringMessageSender(String host, int port) {
    super(host, port);
  }

  @Override
  public void sendMessage(IMessage message) {
    PrintWriter writer;
    try {
      writer = new PrintWriter(new BufferedWriter(
          new OutputStreamWriter(m_outStream, "UTF-8")), //$NON-NLS-1$
          false /* autoflush */);
    } catch (UnsupportedEncodingException e1) {
      writer = new PrintWriter(new BufferedWriter(
          new OutputStreamWriter(m_outStream)),
          false /* autoflush */);
    }

    String msg = ((IStringMessage) message).getMessageAsString();
    if (RemoteTestNG.isVerbose()) {
      p(msg);

      StringBuffer buf = new StringBuffer();
      for(int i = 0; i < msg.length(); i++) {
        if('\u0001' == msg.charAt(i)) {
          p("word:[" + buf.toString() + "]");
          buf.delete(0, buf.length());
        }
        else {
          buf.append(msg.charAt(i));
        }
      }
      p("word:[" + buf.toString() + "]");
    }

    synchronized(m_lock) {
      writer.println(msg);
      writer.flush();
      try {
        m_lock.wait();
      }
      catch(InterruptedException e) { }
    }
  }

  private static void p(String msg) {
    if (RemoteTestNG.isVerbose()) {
      System.out.println("[StringMessageSender] " + msg); //$NON-NLS-1$
    }
  }

}
