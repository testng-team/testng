package test;

import org.testng.remote.strprotocol.IMessage;
import org.testng.remote.strprotocol.SerializedMessageSender;

import java.io.IOException;

public class RemoteTest {

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    SerializedMessageSender sms = new SerializedMessageSender("localhost", 12345);
    sms.initReceiver();
    IMessage message = sms.receiveMessage();
    while (message != null) {
      System.out.println(message);
      message = sms.receiveMessage();
    }
  }
}
