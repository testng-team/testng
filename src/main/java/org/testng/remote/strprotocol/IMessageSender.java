package org.testng.remote.strprotocol;

import java.io.PrintWriter;

public interface IMessageSender {

  void send(IMessage message, Object lock, PrintWriter outStream);

}
