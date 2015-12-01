package org.testng.remote.strprotocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.testng.TestNGException;

import static org.testng.remote.RemoteTestNG.isVerbose;

abstract public class BaseMessageSender implements IMessageSender {
  private boolean m_debug = false;
  protected Socket m_clientSocket;
  private String m_host;
  private int m_port;
  protected final Object m_ackLock = new Object();

  private boolean m_requestStopReceiver;
  /** Outgoing message stream. */
  protected OutputStream m_outStream;
  /** Used to send ACK and STOP */
  private PrintWriter m_outWriter;

  /** Incoming message stream. */
  protected volatile InputStream m_inStream;
  /** Used to receive ACK and STOP */
  protected volatile BufferedReader m_inReader;

  private ReaderThread m_readerThread;
  private boolean m_ack;
//  protected InputStream m_receiverInputStream;

  public BaseMessageSender(String host, int port, boolean ack) {
    m_host = host;
    m_port = port;
    m_ack = ack;
  }

  /**
   * Starts the connection.
   *
   * @throws TestNGException if an exception occurred while establishing the connection
   */
  @Override
  public void connect() throws IOException {
    p("Waiting for Eclipse client on " + m_host + ":" + m_port);
    while (true) {
      try {
        m_clientSocket = new Socket(m_host, m_port);
        p("Received a connection from Eclipse on " + m_host + ":" + m_port);

        // Output streams
        m_outStream = m_clientSocket.getOutputStream();
        m_outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(m_outStream)));

        // Input streams
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
        catch(InterruptedException handled) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private void sendAdminMessage(String message) {
    m_outWriter.println(message);
    m_outWriter.flush();
  }

  private int m_serial = 0;

  @Override
  public void sendAck() {
    p("Sending ACK " + m_serial);
    // Note: adding the serial at the end of this message causes a lock up if interacting
    // with TestNG 5.14 and older (reported by JetBrains). The following git commit:
    // 5730bdfb33ec7a8bf4104852cd4a5f2875ba8267
    // changed equals() to startsWith().
    // It's ok to add this serial back for debugging, but don't commit it until JetBrains
    // confirms they no longer need backward compatibility with 5.14.
    sendAdminMessage(MessageHelper.ACK_MSG); // + m_serial++);
  }

  @Override
  public void sendStop() {
    sendAdminMessage(MessageHelper.STOP_MSG);
  }

  @Override
  public void initReceiver() throws SocketTimeoutException {
    if (m_inStream != null) {
      p("Receiver already initialized");
    }
    ServerSocket serverSocket = null;
    try {
      p("initReceiver on port " + m_port);
      serverSocket = new ServerSocket(m_port);
      serverSocket.setSoTimeout(5000);

      Socket socket = null;
      while (!m_requestStopReceiver) {
        try {
          if (m_debug) {
            p("polling the client connection");
          }
          socket = serverSocket.accept();
          // break the loop once the first client connected
          break;
        }
        catch (IOException ioe) {
          try {
            Thread.sleep(100L);
          }
          catch (InterruptedException ie) {
            // Do nothing.
          }
        }
      }
      if (socket != null) {
        m_inStream = socket.getInputStream();
        m_inReader = new BufferedReader(new InputStreamReader(m_inStream));
        m_outStream = socket.getOutputStream();
        m_outWriter = new PrintWriter(new OutputStreamWriter(m_outStream));
      }
    }
    catch(SocketTimeoutException ste) {
      throw ste;
    }
    catch (IOException ioe) {
      closeQuietly(serverSocket);
    }
  }

  public void stopReceiver() {
    m_requestStopReceiver = true;
  }

  @Override
  public void shutDown() {
    closeQuietly(m_outStream);
    m_outStream = null;

    if (null != m_readerThread) {
      m_readerThread.interrupt();
    }

    closeQuietly(m_inReader);
    m_inReader = null;

    closeQuietly(m_clientSocket);
    m_clientSocket = null;
  }

  private void closeQuietly(Closeable c) {
    if (c != null) {
      try {
        c.close();
      } catch (IOException e) {
        if (m_debug) {
          e.printStackTrace();
        }
      }
    }
  }

  private String m_latestAck;

  protected void waitForAck() {
    if (m_ack) {
      try {
        p("Message sent, waiting for ACK...");
        synchronized(m_ackLock) {
          m_ackLock.wait();
        }
        p("... ACK received:" + m_latestAck);
      }
      catch(InterruptedException handled) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static void p(String msg) {
    if (isVerbose()) {
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
        p("ReaderThread waiting for an admin message");
        String message = m_inReader.readLine();
        p("ReaderThread received admin message:" + message);
        while (message != null) {
          if (m_debug) {
            p("Admin message:" + message); //$NON-NLS-1$
          }
          boolean acknowledge = message.startsWith(MessageHelper.ACK_MSG);
          boolean stop = MessageHelper.STOP_MSG.equals(message);
          if(acknowledge || stop) {
            if (acknowledge) {
              p("Received ACK:" + message);
              m_latestAck = message;
            }
            synchronized(m_ackLock) {
              m_ackLock.notifyAll();
            }
            if (stop) {
              break;
            }
          } else {
            p("Received unknown message: '" + message + "'");
          }
          message = m_inReader != null ? m_inReader.readLine() : null;
        }
//        while((m_reader != null) && (message = m_reader.readLine()) != null) {
//          if (m_debug) {
//            p("Admin message:" + message); //$NON-NLS-1$
//          }
//          boolean acknowledge = MessageHelper.ACK_MSG.equals(message);
//          boolean stop = MessageHelper.STOP_MSG.equals(message);
//          if(acknowledge || stop) {
//            synchronized(m_lock) {
//              m_lock.notifyAll();
//            }
//            if (stop) {
//              break;
//            }
//          }
//        }
      }
      catch(IOException ioe) {
        if (isVerbose()) {
          ioe.printStackTrace();
        }
      }
    }
  }
}
