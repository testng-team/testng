package org.testng.remote.strprotocol;

import org.testng.TestNGException;
import org.testng.remote.RemoteTestNG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

abstract public class BaseMessageSender implements IMessageSender {
  private boolean m_debug = false;
  protected Socket m_clientSocket;
  private String m_host;
  private int m_port;
  protected Object m_lock = new Object();

  /** Outgoing message stream. */
  protected OutputStream m_outStream;

  /** Incoming message stream. */
  protected volatile InputStream m_inStream;
  protected BufferedReader m_inReader;

  private ReaderThread m_readerThread;
  protected InputStream m_receiverInputStream;

  public BaseMessageSender(String host, int port) {
    m_host = host;
    m_port = port;
  }

  /**
   * Starts the connection.
   *
   * @return <TT>true</TT> if the connection was successful, <TT>false</TT> otherwise
   * @throws TestNGException if an exception occurred while establishing the connection
   */
  @Override
  public void connect() throws IOException {
    while (true) {
      p("Waiting for Eclipse client on " + m_host + ":" + m_port);
      try {
        m_clientSocket = new Socket(m_host, m_port);
  
        m_outStream = m_clientSocket.getOutputStream();
        m_inStream = m_clientSocket.getInputStream();

        try {
          m_inReader = new BufferedReader(new InputStreamReader(m_inStream,
              "UTF-8")); //$NON-NLS-1$
        }
        catch(UnsupportedEncodingException ueex) {
          // Should never happen
          m_inReader = new BufferedReader(new InputStreamReader(m_inStream));
        }
  
        p("Connection established, starting reader thread");
        m_readerThread = new ReaderThread();
        m_readerThread.start();
        return;
      }
      catch(ConnectException ex) {
        // ignore and retry
        try {
          Thread.sleep(4000);
        }
        catch(InterruptedException e) {
          ;
        }
      }
    }
  }

  @Override
  public void initReceiver() {
    if (m_receiverInputStream != null) {
      p("Receiver already initialized");
    }
    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(m_port);
      m_receiverInputStream = serverSocket.accept().getInputStream();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void shutDown() {
    if(null != m_outStream) {
      try {
        m_outStream.close();
      }
      catch(IOException ex) {
        // ignore
      }
      m_outStream = null;
    }

    try {
      if(null != m_readerThread) {
        m_readerThread.interrupt();
      }

      if(null != m_inReader) {
        m_inReader.close();
        m_inReader = null;
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
      if(m_debug) {
        e.printStackTrace();
      }
    }
  }

  private static void p(String msg) {
    if (RemoteTestNG.isVerbose()) {
      System.out.println("[BaseMessageSender] " + msg); //$NON-NLS-1$
    }
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
        while((m_inReader != null) && (message = m_inReader.readLine()) != null) {
          if(m_debug) {
            p("Reply:" + message); //$NON-NLS-1$
          }
          boolean acknowledge = MessageHelper.ACK_MSG.equals(message);
          boolean stop = MessageHelper.STOP_MSG.equals(message);
          if(acknowledge || stop) {
            synchronized(m_lock) {
              m_lock.notifyAll();
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
