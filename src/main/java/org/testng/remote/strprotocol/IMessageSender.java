package org.testng.remote.strprotocol;

import java.io.IOException;

public interface IMessageSender {

  void connect() throws IOException;

  void sendMessage(IMessage message) throws Exception;

  void shutDown();
}
