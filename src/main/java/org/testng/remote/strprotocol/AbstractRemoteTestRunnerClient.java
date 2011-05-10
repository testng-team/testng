/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Julien Ruaux: jruaux@octo.com
 *     Vincent Massol: vmassol@octo.com
 *
 * Adapted by:
 *     Alexandru Popescu: the_mindstorm@evolva.ro
 ******************************************************************************/
package org.testng.remote.strprotocol;


import org.testng.TestNGException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The client side of the RemoteTestRunner. Handles the
 * marshaling of the different messages.
 */
public abstract class AbstractRemoteTestRunnerClient {
  /**
   * An array of listeners that are informed about test events.
   */
  protected IRemoteSuiteListener[] m_suiteListeners;
  protected IRemoteTestListener[] m_testListeners;

  /**
   * The server socket
   */
  private ServerSocket       fServerSocket;
  private Socket             fSocket;
  private ServerConnection m_serverConnection;
//  private PrintWriter        m_outputWriter;
//  private BufferedReader     m_inputReader;

  /**
   * Start listening to a test run. Start a server connection that
   * the RemoteTestRunner can connect to.
   */
  public synchronized void startListening(IRemoteSuiteListener[] suiteListeners,
                                          IRemoteTestListener[] testListeners,
                                          ServerConnection serverConnection) {
    m_suiteListeners= suiteListeners;
    m_testListeners= testListeners;
    m_serverConnection = serverConnection;

    serverConnection.start();
  }

  public IRemoteSuiteListener[] getSuiteListeners() {
    return m_suiteListeners;
  }

  public IRemoteTestListener[] getTestListeners() {
    return m_testListeners;
  }

  private synchronized void shutdown() {
//    if(m_outputWriter != null) {
//      m_outputWriter.close();
//      m_outputWriter = null;
//    }
//    try {
//      if(m_inputReader != null) {
//        m_inputReader.close();
//        m_inputReader = null;
//      }
//    }
//    catch(IOException e) {
//      e.printStackTrace();
//    }
    try {
      if(fSocket != null) {
        fSocket.close();
        fSocket = null;
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    try {
      if(fServerSocket != null) {
        fServerSocket.close();
        fServerSocket = null;
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isRunning() {
    return m_serverConnection.getMessageSender() != null;
  }

  /**
   * Requests to stop the remote test run.
   */
  public synchronized void stopTest() {
    if(isRunning()) {
      m_serverConnection.getMessageSender().sendStop();
      shutdown();
    }
  }

//  private String readMessage(BufferedReader in) throws IOException {
//    return in.readLine();
//  }
//
//  private void receiveMessage(String message) {
//    int messageType = MessageHelper.getMessageType(message);
//
//    try {
//      if(messageType < MessageHelper.SUITE) {
//        // Generic message
//        GenericMessage gm = MessageHelper.unmarshallGenericMessage(message);
//        notifyStart(gm);
//      }
//      else if(messageType < MessageHelper.TEST) {
//        // Suite message
//        SuiteMessage sm = MessageHelper.createSuiteMessage(message);
//        notifySuiteEvents(sm);
//      }
//      else if(messageType < MessageHelper.TEST_RESULT) {
//        // Test message
//        TestMessage tm = MessageHelper.createTestMessage(message);
//        notifyTestEvents(tm);
//      }
//      else {
//        // TestResult message
//        TestResultMessage trm = MessageHelper.unmarshallTestResultMessage(message);
//        notifyResultEvents(trm);
//      }
//    }
//    finally {
//      if(isRunning() && (null != m_outputWriter)) {
//        m_outputWriter.println(MessageHelper.ACK_MSG);
//        m_outputWriter.flush();
//      }
//    }
//  }

  protected abstract void notifyStart(final GenericMessage genericMessage);

  protected abstract void notifySuiteEvents(final SuiteMessage suiteMessage);

  protected abstract void notifyTestEvents(final TestMessage testMessage);

  protected abstract void notifyResultEvents(final TestResultMessage testResultMessage);


  /**
   * Reads the message stream from the RemoteTestRunner
   */
  public abstract class ServerConnection extends Thread {
    private MessageHub m_messageHub;

    public ServerConnection(IMessageSender messageMarshaller) {
      super("TestNG - ServerConnection"); //$NON-NLS-1$
      m_messageHub = new MessageHub(messageMarshaller);
    }

    IMessageSender getMessageSender() {
      return m_messageHub != null ? m_messageHub.getMessageSender() : null;
    }

    @Override
    public void run() {
      try {
        IMessage message = m_messageHub.receiveMessage();
        while (message != null) {
          if (message instanceof GenericMessage) {
            notifyStart((GenericMessage) message);
          }
          else if (message instanceof SuiteMessage) {
            notifySuiteEvents((SuiteMessage) message);
          }
          else if (message instanceof TestMessage) {
            notifyTestEvents((TestMessage) message);
          }
          else if (message instanceof TestResultMessage) {
            notifyResultEvents((TestResultMessage) message);
          }
          else {
            throw new TestNGException("Unknown message type:" + message);
          }
//          if (isRunning()) {
//            m_messageMarshaller.sendAck();
//          }
          message = m_messageHub.receiveMessage();
        }
      }
      finally {
        m_messageHub.shutDown();
        m_messageHub = null;
      }
//      try {
//        fServerSocket = new ServerSocket(fServerPort);
//        fSocket = fServerSocket.accept();
//        try {
//          m_inputReader = new BufferedReader(new InputStreamReader(fSocket.getInputStream(),
//                                                                     "UTF-8")); //$NON-NLS-1$
//        }
//        catch(UnsupportedEncodingException e) {
//          m_inputReader = new BufferedReader(new InputStreamReader(fSocket.getInputStream()));
//        }
//        try {
//          m_outputWriter = new PrintWriter(new OutputStreamWriter(fSocket.getOutputStream(), "UTF-8"),
//                                    true);
//        }
//        catch(UnsupportedEncodingException e1) {
//          m_outputWriter = new PrintWriter(new OutputStreamWriter(fSocket.getOutputStream()), true);
//        }
//        String message;
//        while((m_inputReader != null) && ((message = readMessage(m_inputReader)) != null)) {
//          receiveMessage(message);
//        }
//      }
//      catch(SocketException e) {
//        handleThrowable(e);
//      }
//      catch(IOException e) {
//        handleThrowable(e);
//      }
//      finally {
//        shutdown();
//      }
    }

    protected abstract void handleThrowable(Throwable cause);
  }

}
