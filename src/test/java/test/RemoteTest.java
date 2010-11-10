package test;

import org.testng.remote.RemoteTestNG;
import org.testng.remote.strprotocol.IMessage;
import org.testng.remote.strprotocol.IMessageSender;
import org.testng.remote.strprotocol.MessageHub;
import org.testng.remote.strprotocol.SerializedMessageSender;
import org.testng.remote.strprotocol.StringMessageSender;

/**
 * A simple client that launches RemoteTestNG and then talks to it via the
 * two supported protocols, String and Serialized.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class RemoteTest {

  public static void main(String[] args) throws Exception {
    new RemoteTest().testString();
    new RemoteTest().testSerialized();
  }

  public void testSerialized() {
    runTest("-serport", 12345, new SerializedMessageSender("localhost", 12345));
  }

  public void testString() {
    runTest("-port", 12346, new StringMessageSender("localhost", 12346));
  }

  private void launchRemoteTestNG(final String portArg, final int portValue) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        RemoteTestNG.main(new String[] {
            portArg, Integer.toString(portValue),
            "/Users/cbeust/java/testng/src/test/resources/testng-single.xml"});
        }
      }).start();
  }

  private void runTest(String arg, int portValue, IMessageSender sms) {
    launchRemoteTestNG(arg, portValue);
    MessageHub mh = new MessageHub(sms);
    mh.initReceiver();
    IMessage message = mh.receiveMessage();
    while (message != null) {
      System.out.println("Received message:" + message);
      message = mh.receiveMessage();
      if (message == null) {
        System.out.println("Done");
      }
    }
  }
}
