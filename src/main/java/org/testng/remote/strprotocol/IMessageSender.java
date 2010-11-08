package org.testng.remote.strprotocol;

import java.io.IOException;

public interface IMessageSender {

  void connect() throws IOException;

  void initReceiver();

  void sendMessage(IMessage message) throws Exception;

  IMessage receiveMessage();

  void shutDown();
}
