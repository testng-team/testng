package org.testng.remote.strprotocol;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public interface IMessageSender {

  void connect() throws IOException;

  /**
   * Initialize the receiver.
   * the underlying socket server will be polling until a first client connect.
   *
   * @throws SocketException This exception will be thrown if a connection
   * to the remote TestNG instance could not be established after ten
   * seconds.
   */
  void initReceiver() throws SocketTimeoutException;

  /**
   * Stop the receiver.
   * it provides a way that allow the API invoker to stop the receiver,
   * e.g. break from a dead while loop
   */
  void stopReceiver();

  void sendMessage(IMessage message) throws Exception;

  /**
   * Will return null or throw EOFException when the connection has been severed.
   */
  IMessage receiveMessage() throws Exception;

  void shutDown();

  // These two methods should probably be in a separate class since they should all be
  // the same for implementers of this interface.
  void sendAck();

  void sendStop();
}
