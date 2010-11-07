package org.testng.remote.strprotocol;

import org.testng.remote.RemoteTestNG;

import java.io.PrintWriter;

public class StringMessageSender implements IMessageSender {

  @Override
  public void send(IMessage message, Object lock, PrintWriter outStream) {
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

    synchronized(lock) {
      outStream.println(msg);
      outStream.flush();
      try {
          lock.wait();
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
