package org.testng.remote.strprotocol;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.testng.TestNGException;

/**
 * String based socket based communication.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class StringMessageSenderHelper {

  private boolean        m_debugMode = false;

  private Socket         m_clientSocket;
  private String         m_host;
  private int            m_port;

  /** Outgoing message stream. */
  private PrintWriter    m_outStream;

  /** Ingoing message stream. */
  private volatile BufferedReader m_inStream;

  private ReaderThread   m_readerThread;
  private Object lock = new Object();

  public StringMessageSenderHelper(final String host, final int port) {
    m_host = host;
    m_port = port;
  }

  /**
   * Starts the connection.
   *
   * @return <TT>true</TT> if the connection was successfull, <TT>false</TT> otherwise
   * @throws TestNGException if an exception occured while establishing the connection
   */
  public boolean connect() {
    if(m_debugMode) {
      ppp("trying to connect " + m_host + ":" + m_port); //$NON-NLS-1$ //$NON-NLS-2$
    }
    Exception exception = null;

    for(int i = 1; i < 20; i++) {
      try {
        m_clientSocket = new Socket(m_host, m_port);

        try {
          m_outStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(m_clientSocket.getOutputStream(), "UTF-8")), //$NON-NLS-1$
                                        false /*autoflush*/);
        }
        catch(UnsupportedEncodingException ueex) {
          // Should never happen
          m_outStream = new PrintWriter(new BufferedWriter(
              new OutputStreamWriter(m_clientSocket.getOutputStream())),
                  false /*autoflush*/);
        }

        try {
          m_inStream = new BufferedReader(new InputStreamReader(m_clientSocket.getInputStream(), "UTF-8")); //$NON-NLS-1$
        }
        catch(UnsupportedEncodingException ueex) {
          // Should never happen
          m_inStream = new BufferedReader(new InputStreamReader(m_clientSocket.getInputStream()));
        }

        m_readerThread = new ReaderThread();
        m_readerThread.start();

        return true;
      }
      catch(IOException ioe) {
        exception = ioe;
      }

      try {
        Thread.sleep(2000);
      }
      catch(InterruptedException e) {
        ;
      }
    }

    throw new TestNGException("Cannot establish connection: " + m_host + ":" + m_port, exception);
  }

  /**
   * Shutsdown the connection to the remote test listener.
   */
  public void shutDown() {
    if(null != m_outStream) {
      m_outStream.close();
      m_outStream = null;
    }

    try {
      if(null != m_readerThread) {
        m_readerThread.interrupt();
      }

      if(null != m_inStream) {
        m_inStream.close();
        m_inStream = null;
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }

    try {
      if(null != m_clientSocket) {
        m_clientSocket.close();
        m_clientSocket = null;
      }
    }
    catch(IOException e) {
      if(m_debugMode) {
        e.printStackTrace();
      }
    }
  }

  public void sendMessage(IMessage message) {
    throw new UnsupportedOperationException("This operation is too generic yet.");
  }

  public void sendMessage(IStringMessage message) {
    sendMessage(message.getMessageAsString());
  }
  
  private void sendMessage(String msg) {
    if(null == m_outStream) {
      ppp("WARNING the outputstream is null. Cannot send message.");

      return;
    }

    if(m_debugMode) {
      ppp(msg);
  
      StringBuffer buf = new StringBuffer();
      for(int i = 0; i < msg.length(); i++) {
        if('\u0001' == msg.charAt(i)) {
          ppp("word:[" + buf.toString() + "]");
          buf.delete(0, buf.length());
        }
        else {
          buf.append(msg.charAt(i));
        }
      }
      ppp("word:[" + buf.toString() + "]");
    }

    synchronized(lock) {
      m_outStream.println(msg);
      m_outStream.flush();
      try {
          lock.wait();
      } 
      catch(InterruptedException e) { }
    }
  }

  private static void ppp(String msg) {
//    System.out.println("[StringMessageSenderHelper]: " + msg); //$NON-NLS-1$
  }
  
  /**
   * Reader thread that processes messages from the client.
   */
  private class ReaderThread extends Thread {

    public ReaderThread() {
      super("ReaderThread"); //$NON-NLS-1$
    }

    @Override
    public void run() {
      try {
        String message;
        while((m_inStream != null) && (message = m_inStream.readLine()) != null) {
          if(m_debugMode) {
            ppp("Reply:" + message); //$NON-NLS-1$
          }
          boolean acknowledge = MessageHelper.ACK_MSG.equals(message);
          boolean stop = MessageHelper.STOP_MSG.equals(message);
          if(acknowledge || stop) {
            synchronized(lock) {
              lock.notifyAll();
            }
            if (stop) {
            	break;
            }
          }
        }
      }
      catch(IOException ioe) {
        ;
      }
    }
  }
}
