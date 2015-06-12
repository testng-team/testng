package org.testng.remote.strprotocol;

import org.testng.remote.RemoteTestNG;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

public class StringMessageSender extends BaseMessageSender {

  private PrintWriter writer;

  public StringMessageSender(String host, int port) {
    super(host, port, false /* no ack */);
  }

  public StringMessageSender(String host, int port, boolean ack) {
    super(host, port, ack);
  }

  @Override
  public void sendMessage(IMessage message) {
    if (m_outStream == null) {
      throw new IllegalStateException("Trying to send a message on a shutdown sender");
    }
    if (writer == null) {
      try {
        writer = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(m_outStream, "UTF-8")), //$NON-NLS-1$
            false /* autoflush */);
      } catch (UnsupportedEncodingException e1) {
        writer = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(m_outStream)),
            false /* autoflush */);
      }
    }

    String msg = ((IStringMessage) message).getMessageAsString();
    if (RemoteTestNG.isVerbose()) {
      p("Sending message:" + message);
      p("  String version:" + msg);

      StringBuffer buf = new StringBuffer();
      for(int i = 0; i < msg.length(); i++) {
        if('\u0001' == msg.charAt(i)) {
          p("  word:[" + buf.toString() + "]");
          buf.delete(0, buf.length());
        }
        else {
          buf.append(msg.charAt(i));
        }
      }
      p("  word:[" + buf.toString() + "]");
    }

    synchronized(m_ackLock) {
      writer.println(msg);
      writer.flush();
      waitForAck();
    }
  }

  private static void p(String msg) {
    if (RemoteTestNG.isVerbose()) {
      System.out.println("[StringMessageSender] " + msg); //$NON-NLS-1$
    }
  }

  @Override
  public IMessage receiveMessage() {
    IMessage result = null;

    if (m_inReader == null) {
      try {
        m_inReader = new BufferedReader(new InputStreamReader(m_inStream, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        m_inReader = new BufferedReader(new InputStreamReader(m_inStream));
      }
    }
    try {
//      try {
//        m_outputWriter = new PrintWriter(new OutputStreamWriter(fSocket.getOutputStream(), "UTF-8"),
//                                  true);
//      }
//      catch(UnsupportedEncodingException e1) {
//        m_outputWriter = new PrintWriter(new OutputStreamWriter(fSocket.getOutputStream()), true);
//      }
      result = receiveMessage(m_inReader.readLine());
    } catch(IOException e) {
      handleThrowable(e);
    }

    return result;
//    finally {
//      shutDown();
//      return null;
//    }
  }

  protected void handleThrowable(Throwable cause) {
    if (RemoteTestNG.isVerbose()) {
      cause.printStackTrace();
    }
  }

//  private String readMessage(BufferedReader in) throws IOException {
//    return in.readLine();
//  }

  private IMessage receiveMessage(String message) {
    if (message == null) return null;
    IMessage result = null;

    int messageType = MessageHelper.getMessageType(message);

//    try {
      if(messageType < MessageHelper.SUITE) {
        // Generic message
        result = MessageHelper.unmarshallGenericMessage(message);
      }
      else if(messageType < MessageHelper.TEST) {
        // Suite message
        result = MessageHelper.createSuiteMessage(message);
      }
      else if(messageType < MessageHelper.TEST_RESULT) {
        // Test message
        result = MessageHelper.createTestMessage(message);
      }
      else {
        // TestResult message
        result = MessageHelper.unmarshallTestResultMessage(message);
      }
//    }
//    finally {
//      if(isRunning() && (null != m_outputWriter)) {
//        m_outputWriter.println(MessageHelper.ACK_MSG);
//        m_outputWriter.flush();
//      }
//    }

    p("receiveMessage() received:" + result);
    return result;
  }
}
